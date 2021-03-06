package com.linbit.linstor.dbdrivers;

import com.google.inject.AbstractModule;

import com.linbit.linstor.LuksLayerGenericDbDriver;
import com.linbit.linstor.DrbdLayerGenericDbDriver;
import com.linbit.linstor.KeyValueStoreDataGenericDbDriver;
import com.linbit.linstor.NetInterfaceDataGenericDbDriver;
import com.linbit.linstor.NodeConnectionDataGenericDbDriver;
import com.linbit.linstor.NodeDataGenericDbDriver;
import com.linbit.linstor.ResourceConnectionDataGenericDbDriver;
import com.linbit.linstor.ResourceDataGenericDbDriver;
import com.linbit.linstor.ResourceDefinitionDataGenericDbDriver;
import com.linbit.linstor.ResourceLayerIdGenericDbDriver;
import com.linbit.linstor.SnapshotDataGenericDbDriver;
import com.linbit.linstor.SnapshotDefinitionDataGenericDbDriver;
import com.linbit.linstor.SnapshotVolumeDataGenericDbDriver;
import com.linbit.linstor.SnapshotVolumeDefinitionGenericDbDriver;
import com.linbit.linstor.StorPoolDataGenericDbDriver;
import com.linbit.linstor.StorPoolDefinitionDataGenericDbDriver;
import com.linbit.linstor.StorageLayerGenericDbDriver;
import com.linbit.linstor.SwordfishLayerGenericDbDriver;
import com.linbit.linstor.VolumeConnectionDataGenericDbDriver;
import com.linbit.linstor.VolumeDataGenericDbDriver;
import com.linbit.linstor.VolumeDefinitionDataGenericDbDriver;
import com.linbit.linstor.dbdrivers.interfaces.LuksLayerDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.DrbdLayerDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.KeyValueStoreDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.NetInterfaceDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.NodeConnectionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.NodeDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.PropsConDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.ResourceConnectionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.ResourceDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.ResourceDefinitionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.ResourceLayerIdDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.SnapshotDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.SnapshotDefinitionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.SnapshotVolumeDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.SnapshotVolumeDefinitionDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.StorPoolDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.StorPoolDefinitionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.StorageLayerDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.SwordfishLayerDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.VolumeConnectionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.VolumeDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.VolumeDefinitionDataDatabaseDriver;
import com.linbit.linstor.propscon.PropsConGenericDbDriver;
import com.linbit.linstor.security.DbAccessor;
import com.linbit.linstor.security.DbPersistence;
import com.linbit.linstor.security.ObjectProtectionDatabaseDriver;
import com.linbit.linstor.security.ObjectProtectionGenericDbDriver;

public class ControllerDbModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(DbAccessor.class).to(DbPersistence.class);

        bind(ObjectProtectionDatabaseDriver.class).to(ObjectProtectionGenericDbDriver.class);

        bind(DatabaseDriver.class).to(GenericDbDriver.class);

        bind(PropsConDatabaseDriver.class).to(PropsConGenericDbDriver.class);
        bind(NodeDataDatabaseDriver.class).to(NodeDataGenericDbDriver.class);
        bind(ResourceDefinitionDataDatabaseDriver.class).to(ResourceDefinitionDataGenericDbDriver.class);
        bind(ResourceDataDatabaseDriver.class).to(ResourceDataGenericDbDriver.class);
        bind(VolumeDefinitionDataDatabaseDriver.class).to(VolumeDefinitionDataGenericDbDriver.class);
        bind(VolumeDataDatabaseDriver.class).to(VolumeDataGenericDbDriver.class);
        bind(StorPoolDefinitionDataDatabaseDriver.class).to(StorPoolDefinitionDataGenericDbDriver.class);
        bind(StorPoolDataDatabaseDriver.class).to(StorPoolDataGenericDbDriver.class);
        bind(NetInterfaceDataDatabaseDriver.class).to(NetInterfaceDataGenericDbDriver.class);
        bind(NodeConnectionDataDatabaseDriver.class).to(NodeConnectionDataGenericDbDriver.class);
        bind(ResourceConnectionDataDatabaseDriver.class).to(ResourceConnectionDataGenericDbDriver.class);
        bind(VolumeConnectionDataDatabaseDriver.class).to(VolumeConnectionDataGenericDbDriver.class);
        bind(SnapshotDefinitionDataDatabaseDriver.class).to(SnapshotDefinitionDataGenericDbDriver.class);
        bind(SnapshotVolumeDefinitionDatabaseDriver.class).to(SnapshotVolumeDefinitionGenericDbDriver.class);
        bind(SnapshotDataDatabaseDriver.class).to(SnapshotDataGenericDbDriver.class);
        bind(SnapshotVolumeDataDatabaseDriver.class).to(SnapshotVolumeDataGenericDbDriver.class);
        bind(KeyValueStoreDataDatabaseDriver.class).to(KeyValueStoreDataGenericDbDriver.class);

        bind(ResourceLayerIdDatabaseDriver.class).to(ResourceLayerIdGenericDbDriver.class);
        bind(DrbdLayerDatabaseDriver.class).to(DrbdLayerGenericDbDriver.class);
        bind(LuksLayerDatabaseDriver.class).to(LuksLayerGenericDbDriver.class);
        bind(StorageLayerDatabaseDriver.class).to(StorageLayerGenericDbDriver.class);
        bind(SwordfishLayerDatabaseDriver.class).to(SwordfishLayerGenericDbDriver.class);
    }
}
