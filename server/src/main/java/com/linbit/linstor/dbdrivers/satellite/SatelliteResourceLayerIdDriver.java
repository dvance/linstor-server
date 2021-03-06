package com.linbit.linstor.dbdrivers.satellite;

import com.linbit.SingleColumnDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.ResourceLayerIdDatabaseDriver;
import com.linbit.linstor.storage.AbsRscData;
import com.linbit.linstor.storage.interfaces.categories.RscLayerObject;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SatelliteResourceLayerIdDriver implements ResourceLayerIdDatabaseDriver
{
    private final SingleColumnDatabaseDriver<?, ?> noopSingleColDriver = new SatelliteSingleColDriver<>();

    @Inject
    public SatelliteResourceLayerIdDriver()
    {
    }

    @Override
    public void delete(RscLayerObject rscLayerObjectRef)
    {
        // no-op
    }

    @SuppressWarnings("unchecked")
    @Override
    public SingleColumnDatabaseDriver<AbsRscData, RscLayerObject> getParentDriver()
    {
        return (SingleColumnDatabaseDriver<AbsRscData, RscLayerObject>) noopSingleColDriver;
    }

    @Override
    public void persist(RscLayerObject rscLayerObjectRef)
    {
        // no-op
    }
}
