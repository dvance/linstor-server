package com.linbit.linstor.core.apicallhandler.controller;

import com.linbit.linstor.NodeName;
import com.linbit.linstor.ResourceDefinition;
import com.linbit.linstor.ResourceDefinitionRepository;
import com.linbit.linstor.ResourceName;
import com.linbit.linstor.Snapshot;
import com.linbit.linstor.SnapshotDefinition;
import com.linbit.linstor.SnapshotName;
import com.linbit.linstor.SnapshotVolumeDefinition;
import com.linbit.linstor.VolumeNumber;
import com.linbit.linstor.annotation.PeerContext;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.apicallhandler.response.ApiOperation;
import com.linbit.linstor.core.apicallhandler.response.ResponseContext;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Singleton
public class CtrlSnapshotApiCallHandler
{
    private final ResourceDefinitionRepository resourceDefinitionRepository;
    private final Provider<AccessContext> peerAccCtx;

    @Inject
    public CtrlSnapshotApiCallHandler(
        ResourceDefinitionRepository resourceDefinitionRepositoryRef,
        @PeerContext Provider<AccessContext> peerAccCtxRef
    )
    {
        resourceDefinitionRepository = resourceDefinitionRepositoryRef;
        peerAccCtx = peerAccCtxRef;
    }

    ArrayList<SnapshotDefinition.SnapshotDfnListItemApi> listSnapshotDefinitions()
    {
        ArrayList<SnapshotDefinition.SnapshotDfnListItemApi> snapshotDfns = new ArrayList<>();
        try
        {
            for (ResourceDefinition rscDfn : resourceDefinitionRepository.getMapForView(peerAccCtx.get()).values())
            {
                for (SnapshotDefinition snapshotDfn : rscDfn.getSnapshotDfns(peerAccCtx.get()))
                {
                    try
                    {
                        snapshotDfns.add(snapshotDfn.getListItemApiData(peerAccCtx.get()));
                    }
                    catch (AccessDeniedException accDeniedExc)
                    {
                        // don't add snapshot definition without access
                    }
                }
            }
        }
        catch (AccessDeniedException accDeniedExc)
        {
            // for now return an empty list.
        }

        return snapshotDfns;
    }

    public static String getSnapshotDescription(
        List<String> nodeNameStrs,
        String rscNameStr,
        String snapshotNameStr
    )
    {
        String snapshotDescription = "Resource: " + rscNameStr + ", Snapshot: " + snapshotNameStr;
        return nodeNameStrs.isEmpty() ?
            snapshotDescription :
            "Nodes: " + String.join(", ", nodeNameStrs) + "; " + snapshotDescription;
    }

    public static String getSnapshotDescriptionInline(Snapshot snapshot)
    {
        return getSnapshotDescriptionInline(
            Collections.singletonList(snapshot.getNode().getName().displayValue),
            snapshot.getResourceName().displayValue,
            snapshot.getSnapshotName().displayValue
        );
    }

    public static String getSnapshotDescriptionInline(
        List<String> nodeNameStrs,
        String rscNameStr,
        String snapshotNameStr
    )
    {
        String snapshotDescription = getSnapshotDfnDescriptionInline(rscNameStr, snapshotNameStr);
        return nodeNameStrs.isEmpty() ?
            snapshotDescription :
            snapshotDescription + " on nodes '" + String.join(", ", nodeNameStrs) + "'";
    }

    public static String getSnapshotDfnDescription(String snapshotName)
    {
        return "Snapshot definition: " + snapshotName;
    }

    public static String getSnapshotDfnDescriptionInline(SnapshotDefinition snapshotDfn)
    {
        return getSnapshotDfnDescriptionInline(snapshotDfn.getResourceName(), snapshotDfn.getName());
    }

    public static String getSnapshotDfnDescriptionInline(
        ResourceName rscName,
        SnapshotName snapshotName
    )
    {
        return getSnapshotDfnDescriptionInline(rscName.displayValue, snapshotName.displayValue);
    }

    public static String getSnapshotDfnDescriptionInline(
        String rscNameStr,
        String snapshotNameStr
    )
    {
        return "snapshot '" + snapshotNameStr + "' of resource '" + rscNameStr + "'";
    }

    public static String getSnapshotVlmDfnDescriptionInline(SnapshotVolumeDefinition snapshotVlmDfn)
    {
        return getSnapshotVlmDfnDescriptionInline(
            snapshotVlmDfn.getResourceName().displayValue,
            snapshotVlmDfn.getSnapshotName().displayValue,
            snapshotVlmDfn.getVolumeNumber().value
        );
    }

    public static String getSnapshotVlmDfnDescriptionInline(
        String rscNameStr,
        String snapshotNameStr,
        Integer vlmNr
    )
    {
        return "volume definition with number '" + vlmNr +
            "' of snapshot '" + snapshotNameStr + "' of resource '" + rscNameStr + "'";
    }

    public static String getSnapshotVlmDescriptionInline(
        NodeName nodeName,
        ResourceName resourceName,
        SnapshotName snapshotName,
        VolumeNumber volumeNumber
    )
    {
        return "volume with number '" + volumeNumber.value +
            "' of snapshot '" + snapshotName + "' of resource '" + resourceName + "' on '" + nodeName + "'";
    }

    public static ResponseContext makeSnapshotContext(
        ApiOperation operation,
        List<String> nodeNameStrs,
        String rscNameStr,
        String snapshotNameStr
    )
    {
        Map<String, String> objRefs = new TreeMap<>();
        objRefs.put(ApiConsts.KEY_RSC_DFN, rscNameStr);
        objRefs.put(ApiConsts.KEY_SNAPSHOT, snapshotNameStr);

        return new ResponseContext(
            operation,
            getSnapshotDescription(nodeNameStrs, rscNameStr, snapshotNameStr),
            getSnapshotDescriptionInline(nodeNameStrs, rscNameStr, snapshotNameStr),
            ApiConsts.MASK_SNAPSHOT,
            objRefs
        );
    }
}
