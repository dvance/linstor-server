package com.linbit.linstor.dbdrivers.satellite;

import com.linbit.SingleColumnDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.LuksLayerDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.ResourceLayerIdDatabaseDriver;
import com.linbit.linstor.storage.data.adapter.luks.LuksRscData;
import com.linbit.linstor.storage.data.adapter.luks.LuksVlmData;
import javax.inject.Inject;

import java.sql.SQLException;

public class SatelliteLuksDriver implements LuksLayerDatabaseDriver
{
    private final SingleColumnDatabaseDriver<?, ?> noopSingleColDriver = new SatelliteSingleColDriver<>();
    private final ResourceLayerIdDatabaseDriver noopResourceLayerIdDriver = new SatelliteResourceLayerIdDriver();

    @Inject
    public SatelliteLuksDriver()
    {
    }


    @Override
    public ResourceLayerIdDatabaseDriver getIdDriver()
    {
        return noopResourceLayerIdDriver;
    }

    @Override
    public void persist(LuksRscData luksRscDataRef) throws SQLException
    {
        // no-op
    }

    @Override
    public void delete(LuksRscData luksRscDataRef) throws SQLException
    {
        // no-op
    }

    @Override
    public void persist(LuksVlmData luksVlmDataRef) throws SQLException
    {
        // no-op
    }

    @Override
    public void delete(LuksVlmData luksVlmDataRef) throws SQLException
    {
        // no-op
    }

    @SuppressWarnings("unchecked")
    @Override
    public SingleColumnDatabaseDriver<LuksVlmData, byte[]> getVlmEncryptedPasswordDriver()
    {
        return (SingleColumnDatabaseDriver<LuksVlmData, byte[]>) noopSingleColDriver;
    }
}
