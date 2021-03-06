package com.linbit.linstor.core.apicallhandler.controller;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.linstor.Node;
import com.linbit.linstor.NodeRepository;
import com.linbit.linstor.Resource;
import com.linbit.linstor.ResourceConnection;
import com.linbit.linstor.ResourceConnectionKey;
import com.linbit.linstor.ResourceData;
import com.linbit.linstor.ResourceDefinition;
import com.linbit.linstor.ResourceDefinitionRepository;
import com.linbit.linstor.ResourceName;
import com.linbit.linstor.annotation.ApiContext;
import com.linbit.linstor.annotation.PeerContext;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.pojo.RscConnPojo;
import com.linbit.linstor.api.prop.LinStorObject;
import com.linbit.linstor.core.apicallhandler.controller.helpers.ResourceList;
import com.linbit.linstor.core.apicallhandler.controller.internal.CtrlSatelliteUpdater;
import com.linbit.linstor.core.apicallhandler.response.ApiAccessDeniedException;
import com.linbit.linstor.core.apicallhandler.response.ApiOperation;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.core.apicallhandler.response.ApiSuccessUtils;
import com.linbit.linstor.core.apicallhandler.response.ResponseContext;
import com.linbit.linstor.core.apicallhandler.response.ResponseConverter;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.satellitestate.SatelliteState;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;

import static com.linbit.linstor.core.apicallhandler.controller.CtrlRscDfnApiCallHandler.getRscDfnDescriptionInline;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import static java.util.stream.Collectors.toList;

@Singleton
public class CtrlRscApiCallHandler
{
    private final ErrorReporter errorReporter;
    private final AccessContext apiCtx;
    private final CtrlTransactionHelper ctrlTransactionHelper;
    private final CtrlPropsHelper ctrlPropsHelper;
    private final CtrlApiDataLoader ctrlApiDataLoader;
    private final ResourceDefinitionRepository resourceDefinitionRepository;
    private final NodeRepository nodeRepository;
    private final CtrlSatelliteUpdater ctrlSatelliteUpdater;
    private final ResponseConverter responseConverter;
    private final Provider<Peer> peer;
    private final Provider<AccessContext> peerAccCtx;

    @Inject
    public CtrlRscApiCallHandler(
        ErrorReporter errorReporterRef,
        @ApiContext AccessContext apiCtxRef,
        CtrlTransactionHelper ctrlTransactionHelperRef,
        CtrlPropsHelper ctrlPropsHelperRef,
        CtrlApiDataLoader ctrlApiDataLoaderRef,
        ResourceDefinitionRepository resourceDefinitionRepositoryRef,
        NodeRepository nodeRepositoryRef,
        CtrlSatelliteUpdater ctrlSatelliteUpdaterRef,
        ResponseConverter responseConverterRef,
        Provider<Peer> peerRef,
        @PeerContext Provider<AccessContext> peerAccCtxRef
    )
    {
        errorReporter = errorReporterRef;
        apiCtx = apiCtxRef;
        ctrlTransactionHelper = ctrlTransactionHelperRef;
        ctrlPropsHelper = ctrlPropsHelperRef;
        ctrlApiDataLoader = ctrlApiDataLoaderRef;
        resourceDefinitionRepository = resourceDefinitionRepositoryRef;
        nodeRepository = nodeRepositoryRef;
        ctrlSatelliteUpdater = ctrlSatelliteUpdaterRef;
        responseConverter = responseConverterRef;
        peer = peerRef;
        peerAccCtx = peerAccCtxRef;
    }

    public ApiCallRc modifyResource(
        UUID rscUuid,
        String nodeNameStr,
        String rscNameStr,
        Map<String, String> overrideProps,
        Set<String> deletePropKeys,
        Set<String> deletePropNamespacesRef
    )
    {
        ApiCallRcImpl responses = new ApiCallRcImpl();
        ResponseContext context = makeRscContext(
            ApiOperation.makeModifyOperation(),
            nodeNameStr,
            rscNameStr
        );

        try
        {
            ResourceData rsc = ctrlApiDataLoader.loadRsc(nodeNameStr, rscNameStr, true);

            if (rscUuid != null && !rscUuid.equals(rsc.getUuid()))
            {
                throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                    ApiConsts.FAIL_UUID_RSC,
                    "UUID-check failed"
                ));
            }

            Props props = ctrlPropsHelper.getProps(rsc);

            ctrlPropsHelper.fillProperties(LinStorObject.RESOURCE, overrideProps, props, ApiConsts.FAIL_ACC_DENIED_RSC);
            ctrlPropsHelper.remove(props, deletePropKeys, deletePropNamespacesRef);

            // check if specified preferred network interface exists
            ctrlPropsHelper.checkPrefNic(
                    apiCtx,
                    rsc.getAssignedNode(),
                    overrideProps.get(ApiConsts.KEY_STOR_POOL_PREF_NIC),
                    ApiConsts.MASK_RSC
            );

            ctrlTransactionHelper.commit();

            responseConverter.addWithDetail(responses, context, ctrlSatelliteUpdater.updateSatellites(rsc));
            responseConverter.addWithOp(responses, context, ApiSuccessUtils.defaultModifiedEntry(
                rsc.getUuid(), getRscDescriptionInline(rsc)));
        }
        catch (Exception | ImplementationError exc)
        {
            responses = responseConverter.reportException(peer.get(), context, exc);
        }

        return responses;
    }

    ResourceList listResources(
        String rscNameStr,
        List<String> filterNodes
    )
    {
        // fake load and fail if not exists
        ctrlApiDataLoader.loadRscDfn(rscNameStr, true);

        List<String> rscList = new ArrayList<>();
        rscList.add(rscNameStr);
        return listResources(filterNodes, rscList);
    }

    ResourceList listResources(
        List<String> filterNodes,
        List<String> filterResources
    )
    {
        final ResourceList rscList = new ResourceList();
        try
        {
            final List<String> upperFilterNodes = filterNodes.stream().map(String::toUpperCase).collect(toList());
            final List<String> upperFilterResources =
                filterResources.stream().map(String::toUpperCase).collect(toList());

            resourceDefinitionRepository.getMapForView(peerAccCtx.get()).values().stream()
                .filter(rscDfn -> upperFilterResources.isEmpty() ||
                    upperFilterResources.contains(rscDfn.getName().value))
                .forEach(rscDfn ->
                {
                    try
                    {
                        for (Resource rsc : rscDfn.streamResource(peerAccCtx.get())
                            .filter(rsc -> upperFilterNodes.isEmpty() ||
                                upperFilterNodes.contains(rsc.getAssignedNode().getName().value))
                            .collect(toList()))
                        {
                            rscList.addResource(rsc.getApiData(peerAccCtx.get(), null, null));
                            // fullSyncId and updateId null, as they are not going to be serialized anyways
                        }
                    }
                    catch (AccessDeniedException accDeniedExc)
                    {
                        // don't add storpooldfn without access
                    }
                }
                );

            // get resource states of all nodes
            for (final Node node : nodeRepository.getMapForView(peerAccCtx.get()).values())
            {
                if (upperFilterNodes.isEmpty() || upperFilterNodes.contains(node.getName().value))
                {
                    final Peer curPeer = node.getPeer(peerAccCtx.get());
                    if (curPeer != null)
                    {
                        Lock readLock = curPeer.getSatelliteStateLock().readLock();
                        readLock.lock();
                        try
                        {
                            final SatelliteState satelliteState = curPeer.getSatelliteState();

                            if (satelliteState != null)
                            {
                                final SatelliteState filterStates = new SatelliteState(satelliteState);

                                // states are already complete, we remove all resource that are not interesting from
                                // our clone
                                Set<ResourceName> removeSet = new TreeSet<>();
                                for (ResourceName rscName : filterStates.getResourceStates().keySet())
                                {
                                    if (!(upperFilterResources.isEmpty() ||
                                          upperFilterResources.contains(rscName.value)))
                                    {
                                        removeSet.add(rscName);
                                    }
                                }
                                removeSet.forEach(rscName -> filterStates.getResourceStates().remove(rscName));
                                rscList.putSatelliteState(node.getName(), filterStates);
                            }
                        }
                        finally
                        {
                            readLock.unlock();
                        }
                    }
                }
            }
        }
        catch (AccessDeniedException accDeniedExc)
        {
            // for now return an empty list.
            errorReporter.reportError(accDeniedExc);
        }

        return rscList;
    }

    List<ResourceConnection.RscConnApi> listResourceConnections(
        final String rscNameString
    )
    {
        ResourceName rscName = null;
        List<ResourceConnection.RscConnApi> rscConns = new ArrayList<>();
        try
        {
            rscName = new ResourceName(rscNameString);

            ResourceDefinition rscDfn = resourceDefinitionRepository.get(apiCtx, rscName);

            if (rscDfn  != null)
            {
                for (Resource rsc : rscDfn.streamResource(apiCtx).collect(toList()))
                {
                    List<ResourceConnection> rscConnections = rsc.streamResourceConnections(apiCtx).collect(toList());
                    for (ResourceConnection rscConn : rscConnections)
                    {
                        if (rscConns.stream().noneMatch(con -> con.getUuid() == rscConn.getUuid()))
                        {
                            rscConns.add(rscConn.getApiData(apiCtx));
                        }
                    }
                }

                // lazy instance other resource connections
                List<Resource> resourceList = rscDfn.streamResource(apiCtx).collect(toList());
                for (int i = 0; i < resourceList.size(); i++)
                {
                    for (int j = 1; j < resourceList.size(); j++)
                    {
                        ResourceConnectionKey conKey =
                            new ResourceConnectionKey(resourceList.get(i), resourceList.get(j));

                        if (conKey.getSource() != conKey.getTarget() &&
                            rscConns.stream().noneMatch(con ->
                                con.getSourceNodeName().equalsIgnoreCase(
                                    conKey.getSource().getAssignedNode().getName().getName()) &&
                                con.getTargetNodeName().equalsIgnoreCase(
                                    conKey.getTarget().getAssignedNode().getName().getName()) &&
                                con.getResourceName().equalsIgnoreCase(rscNameString)))
                        {
                            rscConns.add(new RscConnPojo(
                                UUID.randomUUID(),
                                conKey.getSource().getAssignedNode().getName().getDisplayName(),
                                conKey.getTarget().getAssignedNode().getName().getDisplayName(),
                                rscDfn.getName().getDisplayName(),
                                new HashMap<>(),
                                0,
                                null
                            ));
                        }
                    }
                }
            }
            else
            {
                throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                    ApiConsts.FAIL_NOT_FOUND_RSC_DFN,
                    String.format("Resource definition '%s' not found.", rscNameString)
                ));
            }
        }
        catch (InvalidNameException exc)
        {
            throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                ApiConsts.FAIL_INVLD_RSC_NAME,
                "Invalid resource name used"
            ), exc);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "access " + getRscDfnDescriptionInline(rscName.displayValue),
                ApiConsts.FAIL_ACC_DENIED_RSC_DFN
            );
        }

        return rscConns;
    }

    public static String getRscDescription(Resource resource)
    {
        return getRscDescription(
            resource.getAssignedNode().getName().displayValue, resource.getDefinition().getName().displayValue);
    }

    public static String getRscDescription(String nodeNameStr, String rscNameStr)
    {
        return "Node: " + nodeNameStr + ", Resource: " + rscNameStr;
    }

    public static String getRscDescriptionInline(Resource rsc)
    {
        return getRscDescriptionInline(rsc.getAssignedNode(), rsc.getDefinition());
    }

    public static String getRscDescriptionInline(Node node, ResourceDefinition rscDfn)
    {
        return getRscDescriptionInline(node.getName().displayValue, rscDfn.getName().displayValue);
    }

    public static String getRscDescriptionInline(String nodeNameStr, String rscNameStr)
    {
        return "resource '" + rscNameStr + "' on node '" + nodeNameStr + "'";
    }

    static ResponseContext makeRscContext(
        ApiOperation operation,
        String nodeNameStr,
        String rscNameStr
    )
    {
        Map<String, String> objRefs = new TreeMap<>();
        objRefs.put(ApiConsts.KEY_NODE, nodeNameStr);
        objRefs.put(ApiConsts.KEY_RSC_DFN, rscNameStr);

        return new ResponseContext(
            operation,
            getRscDescription(nodeNameStr, rscNameStr),
            getRscDescriptionInline(nodeNameStr, rscNameStr),
            ApiConsts.MASK_RSC,
            objRefs
        );
    }
}
