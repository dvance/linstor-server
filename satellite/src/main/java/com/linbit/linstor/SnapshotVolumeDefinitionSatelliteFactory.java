package com.linbit.linstor;

import com.linbit.ImplementationError;
import com.linbit.linstor.dbdrivers.interfaces.SnapshotVolumeDefinitionDatabaseDriver;
import com.linbit.linstor.propscon.PropsContainerFactory;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.stateflags.StateFlagsBits;
import com.linbit.linstor.transaction.TransactionMgr;
import com.linbit.linstor.transaction.TransactionObjectFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.TreeMap;
import java.util.UUID;

public class SnapshotVolumeDefinitionSatelliteFactory
{
    private final SnapshotVolumeDefinitionDatabaseDriver driver;
    private final PropsContainerFactory propsContainerFactory;
    private final TransactionObjectFactory transObjFactory;
    private final Provider<TransactionMgr> transMgrProvider;

    @Inject
    public SnapshotVolumeDefinitionSatelliteFactory(
        SnapshotVolumeDefinitionDatabaseDriver driverRef,
        PropsContainerFactory propsContainerFactoryRef,
        TransactionObjectFactory transObjFactoryRef,
        Provider<TransactionMgr> transMgrProviderRef
    )
    {
        driver = driverRef;
        propsContainerFactory = propsContainerFactoryRef;
        transObjFactory = transObjFactoryRef;
        transMgrProvider = transMgrProviderRef;
    }

    public SnapshotVolumeDefinition getInstanceSatellite(
        AccessContext accCtx,
        UUID snapshotVolumeDefinitionUuid,
        SnapshotDefinition snapshotDfn,
        VolumeNumber volumeNumber,
        long vlmSize,
        SnapshotVolumeDefinition.SnapshotVlmDfnFlags[] flags
    )
        throws ImplementationError
    {
        SnapshotVolumeDefinition snapshotVolumeDefinition;
        try
        {
            snapshotVolumeDefinition = snapshotDfn.getSnapshotVolumeDefinition(accCtx, volumeNumber);
            if (snapshotVolumeDefinition == null)
            {
                snapshotVolumeDefinition = new SnapshotVolumeDefinitionData(
                    snapshotVolumeDefinitionUuid,
                    snapshotDfn,
                    volumeNumber,
                    vlmSize,
                    StateFlagsBits.getMask(flags),
                    driver,
                    propsContainerFactory,
                    transObjFactory,
                    transMgrProvider,
                    new TreeMap<>()
                );
                snapshotDfn.addSnapshotVolumeDefinition(accCtx, snapshotVolumeDefinition);
            }
        }
        catch (Exception exc)
        {
            throw new ImplementationError(
                "This method should only be called with a satellite db in background!",
                exc
            );
        }
        return snapshotVolumeDefinition;
    }
}
