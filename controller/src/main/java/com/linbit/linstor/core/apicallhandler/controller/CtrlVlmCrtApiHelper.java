package com.linbit.linstor.core.apicallhandler.controller;

import com.linbit.ImplementationError;
import com.linbit.linstor.CtrlStorPoolResolveHelper;
import com.linbit.linstor.LinStorDataAlreadyExistsException;
import com.linbit.linstor.Node;
import com.linbit.linstor.Resource;
import com.linbit.linstor.StorPool;
import com.linbit.linstor.VolumeData;
import com.linbit.linstor.VolumeDataControllerFactory;
import com.linbit.linstor.VolumeDefinition;
import com.linbit.linstor.annotation.ApiContext;
import com.linbit.linstor.annotation.PeerContext;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiCallRcWith;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.core.apicallhandler.response.ApiAccessDeniedException;
import com.linbit.linstor.core.apicallhandler.response.ApiRcException;
import com.linbit.linstor.core.apicallhandler.response.ApiSQLException;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.propscon.InvalidKeyException;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.storage.kinds.DeviceProviderKind;

import static com.linbit.linstor.core.apicallhandler.controller.CtrlVlmListApiCallHandler.getVlmDescriptionInline;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.Map;

@Singleton
public class CtrlVlmCrtApiHelper
{
    private final AccessContext apiCtx;
    private final VolumeDataControllerFactory volumeDataFactory;
    private final Provider<AccessContext> peerAccCtx;
    private final CtrlStorPoolResolveHelper storPoolResolveHelper;

    @Inject
    CtrlVlmCrtApiHelper(
        @ApiContext AccessContext apiCtxRef,
        VolumeDataControllerFactory volumeDataFactoryRef,
        @PeerContext Provider<AccessContext> peerAccCtxRef,
        CtrlStorPoolResolveHelper storPoolResolveHelperRef
    )
    {
        apiCtx = apiCtxRef;
        volumeDataFactory = volumeDataFactoryRef;
        peerAccCtx = peerAccCtxRef;
        storPoolResolveHelper = storPoolResolveHelperRef;
    }

    public ApiCallRcWith<VolumeData> createVolumeResolvingStorPool(
        Resource rsc,
        VolumeDefinition vlmDfn,
        Map<StorPool.Key, Long> thinFreeCapacities
    )
    {
        return createVolumeResolvingStorPool(rsc, vlmDfn, thinFreeCapacities, null, null);
    }

    public ApiCallRcWith<VolumeData> createVolumeResolvingStorPool(
        Resource rsc,
        VolumeDefinition vlmDfn,
        Map<StorPool.Key, Long> thinFreeCapacities,
        String blockDevice,
        String metaDisk
    )
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();
        boolean isDiskless = isDiskless(rsc);
        StorPool storPool = storPoolResolveHelper.resolveStorPool(rsc, vlmDfn, isDiskless).extractApiCallRc(apiCallRc);

        return new ApiCallRcWith<>(apiCallRc, createVolume(
            rsc,
            vlmDfn,
            storPool,
            thinFreeCapacities,
            blockDevice,
            metaDisk
        ));
    }

    public VolumeData createVolume(
        Resource rsc,
        VolumeDefinition vlmDfn,
        StorPool storPool,
        Map<StorPool.Key, Long> thinFreeCapacities,
        String blockDevice,
        String metaDisk
    )
    {
        /*
         * check if StorPool is usable only if
         * - storPool has Backing storage
         * - satellite is online
         * - storPool is Fat-provisioned or we have a map of the thin-free-spaces available
         * - the overrideVlmId property is not set; in this case we assume the volume already
         * exists on the storPool, which means we will not consume additional $volumeSize space
         */

        DeviceProviderKind driverKind = storPool.getDeviceProviderKind();
        if (driverKind.hasBackingDevice() &&
            getPeerPrivileged(rsc.getAssignedNode()).getConnectionStatus() == Peer.ConnectionStatus.ONLINE &&
            (thinFreeCapacities != null || !driverKind.usesThinProvisioning()) &&
            !isOverrideVlmIdPropertySetPrivileged(vlmDfn)
        )
        {
            if (!FreeCapacityAutoPoolSelectorUtils
                .isStorPoolUsable(
                    getVolumeSizePrivileged(vlmDfn),
                    thinFreeCapacities,
                    true,
                    storPool.getName(),
                    rsc.getAssignedNode(),
                    apiCtx
                )
                // allow the volume to be created if the free capacity is unknown
                .orElse(true)
            )
            {
                throw new ApiRcException(
                    ApiCallRcImpl.simpleEntry(
                        ApiConsts.FAIL_INVLD_VLM_SIZE,
                        String.format(
                            "Not enough free space available for volume %d of resource '%s'.",
                            vlmDfn.getVolumeNumber().value,
                            rsc.getDefinition().getName().getDisplayName()
                        )
                    )
                );
            }
        }

        VolumeData vlm;
        try
        {
            vlm = volumeDataFactory.create(
                peerAccCtx.get(),
                rsc,
                vlmDfn,
                storPool,
                null // flags
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ApiAccessDeniedException(
                accDeniedExc,
                "register " + getVlmDescriptionInline(rsc, vlmDfn),
                ApiConsts.FAIL_ACC_DENIED_VLM
            );
        }
        catch (LinStorDataAlreadyExistsException dataAlreadyExistsExc)
        {
            throw new ApiRcException(ApiCallRcImpl.simpleEntry(
                ApiConsts.FAIL_EXISTS_VLM,
                "The " + getVlmDescriptionInline(rsc, vlmDfn) + " already exists"
            ), dataAlreadyExistsExc);
        }
        catch (SQLException sqlExc)
        {
            throw new ApiSQLException(sqlExc);
        }

        return vlm;
    }

    private Peer getPeerPrivileged(Node assignedNode)
    {
        Peer peer;
        try
        {
            peer = assignedNode.getPeer(apiCtx);
        }
        catch (AccessDeniedException exc)
        {
            throw new ImplementationError(exc);
        }
        return peer;
    }

    private boolean isOverrideVlmIdPropertySetPrivileged(VolumeDefinition vlmDfn)
    {
        boolean isSet;
        try
        {
            isSet = vlmDfn.getProps(apiCtx)
                .getProp(ApiConsts.KEY_STOR_POOL_OVERRIDE_VLM_ID) != null;
        }
        catch (AccessDeniedException | InvalidKeyException exc)
        {
            throw new ImplementationError(exc);
        }
        return isSet;
    }



    private long getVolumeSizePrivileged(VolumeDefinition vlmDfn)
    {
        long volumeSize;
        try
        {
            volumeSize = vlmDfn.getVolumeSize(apiCtx);
        }
        catch (AccessDeniedException exc)
        {
            throw new ImplementationError(exc);
        }
        return volumeSize;
    }

    public boolean isDiskless(Resource rsc)
    {
        boolean isDiskless;
        try
        {
            isDiskless = rsc.getStateFlags().isSet(apiCtx, Resource.RscFlags.DISKLESS);
        }
        catch (AccessDeniedException implError)
        {
            throw new ImplementationError(implError);
        }
        return isDiskless;
    }
}
