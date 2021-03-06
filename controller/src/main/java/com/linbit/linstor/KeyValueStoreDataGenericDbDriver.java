package com.linbit.linstor;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.linstor.KeyValueStore.InitMaps;
import com.linbit.linstor.annotation.SystemContext;
import com.linbit.linstor.dbdrivers.derby.DbConstants;
import com.linbit.linstor.dbdrivers.interfaces.KeyValueStoreDataDatabaseDriver;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.propscon.PropsContainerFactory;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.ObjectProtection;
import com.linbit.linstor.security.ObjectProtectionDatabaseDriver;
import com.linbit.linstor.transaction.TransactionMgr;
import com.linbit.linstor.transaction.TransactionObjectFactory;
import com.linbit.utils.Pair;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

@Singleton
public class KeyValueStoreDataGenericDbDriver implements KeyValueStoreDataDatabaseDriver
{
    private static final String TBL_KVS = DbConstants.TBL_KEY_VALUE_STORE;

    private static final String KVS_UUID = DbConstants.UUID;
    private static final String KVS_NAME = DbConstants.KVS_NAME;
    private static final String KVS_DSP_NAME = DbConstants.KVS_DSP_NAME;

    private static final String KVS_SELECT_ALL =
        " SELECT " + KVS_UUID + ", " + KVS_NAME + ", " + KVS_DSP_NAME +
        " FROM " + TBL_KVS;
    private static final String KVS_SELECT =
        KVS_SELECT_ALL +
        " WHERE " + KVS_NAME + " = ?";

    private static final String KVS_INSERT =
        " INSERT INTO " + TBL_KVS +
        " (" + KVS_UUID + ", " + KVS_NAME + ", " + KVS_DSP_NAME + ")" +
        " VALUES (?, ?, ?)";

    private static final String KVS_DELETE =
        " DELETE FROM " + TBL_KVS +
        " WHERE " + KVS_NAME + " = ?";

    private final AccessContext dbCtx;
    private final ErrorReporter errorReporter;

    private final ObjectProtectionDatabaseDriver objProtDriver;
    private final PropsContainerFactory propsContainerFactory;
    private final TransactionObjectFactory transObjFactory;
    private final Provider<TransactionMgr> transMgrProvider;

    @Inject
    public KeyValueStoreDataGenericDbDriver(
        @SystemContext AccessContext accCtx,
        ErrorReporter errorReporterRef,
        ObjectProtectionDatabaseDriver objProtDriverRef,
        PropsContainerFactory propsContainerFactoryRef,
        TransactionObjectFactory transObjFactoryRef,
        Provider<TransactionMgr> transMgrProviderRef
    )
    {
        dbCtx = accCtx;
        errorReporter = errorReporterRef;
        objProtDriver = objProtDriverRef;
        propsContainerFactory = propsContainerFactoryRef;
        transObjFactory = transObjFactoryRef;
        transMgrProvider = transMgrProviderRef;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void create(KeyValueStoreData kvs) throws SQLException
    {
        errorReporter.logTrace("Creating KeyValueStore %s", getId(kvs));
        try (PreparedStatement stmt = getConnection().prepareStatement(KVS_INSERT))
        {
            stmt.setString(1, kvs.getUuid().toString());
            stmt.setString(2, kvs.getName().value);
            stmt.setString(3, kvs.getName().displayValue);
            stmt.executeUpdate();

            errorReporter.logTrace("KeyValueStore created %s", getId(kvs));
        }
    }

    @Override
    public boolean exists(KeyValueStoreName kvsName) throws SQLException
    {
        boolean exists = false;
        try (PreparedStatement stmt = getConnection().prepareStatement(KVS_SELECT))
        {
            stmt.setString(1, kvsName.value);
            try (ResultSet resultSet = stmt.executeQuery())
            {
                exists = resultSet.next();
            }
        }
        return exists;
    }

    public Map<KeyValueStoreData, InitMaps> loadAll() throws SQLException
    {
        errorReporter.logTrace("Loading all KeyValueStores");
        Map<KeyValueStoreData, InitMaps> kvsMap = new TreeMap<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(KVS_SELECT_ALL))
        {
            try (ResultSet resultSet = stmt.executeQuery())
            {
                while (resultSet.next())
                {
                    Pair<KeyValueStoreData, InitMaps> pair = restoreKvs(resultSet);
                    kvsMap.put(pair.objA, pair.objB);
                }
            }
        }
        errorReporter.logTrace("Loaded %d KeyValueStores", kvsMap.size());
        return kvsMap;
    }

    private Pair<KeyValueStoreData, InitMaps> restoreKvs(ResultSet resultSet) throws SQLException
    {
        Pair<KeyValueStoreData, InitMaps> retPair = new Pair<>();
        KeyValueStoreData kvs;
        KeyValueStoreName kvsName;
        try
        {
            kvsName = new KeyValueStoreName(resultSet.getString(KVS_DSP_NAME));
        }
        catch (InvalidNameException invalidNameExc)
        {
            throw new LinStorSqlRuntimeException(
                String.format(
                    "The display name of a stored KeyValueStore in the table %s could not be restored. " +
                        "(invalid display KvsName=%s)",
                    TBL_KVS,
                    resultSet.getString(KVS_DSP_NAME)
                ),
                invalidNameExc
            );
        }

        ObjectProtection objProt = getObjectProtection(kvsName);

        kvs = new KeyValueStoreData(
            java.util.UUID.fromString(resultSet.getString(KVS_UUID)),
            objProt,
            kvsName,
            this,
            propsContainerFactory,
            transObjFactory,
            transMgrProvider
        );

        retPair.objA = kvs;
        retPair.objB = new KvsInitMaps();

        errorReporter.logTrace("KeyValueStore instance created %s", getId(kvs));
        return retPair;
    }

    private ObjectProtection getObjectProtection(KeyValueStoreName kvsName)
        throws SQLException, ImplementationError
    {
        ObjectProtection objProt = objProtDriver.loadObjectProtection(
            ObjectProtection.buildPath(kvsName),
            false // no need to log a warning, as we would fail then anyways
        );
        if (objProt == null)
        {
            throw new ImplementationError(
                "KeyValueStore's DB entry exists, but is missing an entry in ObjProt table! " +
                getId(kvsName), null
            );
        }
        return objProt;
    }

    @Override
    public void delete(KeyValueStoreData kvs) throws SQLException
    {
        errorReporter.logTrace("Deleting KeyValueStore %s", getId(kvs));
        try (PreparedStatement stmt = getConnection().prepareStatement(KVS_DELETE))
        {
            stmt.setString(1, kvs.getName().value);
            stmt.executeUpdate();
        }
        errorReporter.logTrace("KeyValueStore deleted %s", getId(kvs));
    }

    private Connection getConnection()
    {
        return transMgrProvider.get().getConnection();
    }

    private String getId(KeyValueStoreData kvs)
    {
        return getId(kvs.getName().displayValue);
    }

    private String getId(KeyValueStoreName kvsName)
    {
        return getId(kvsName.displayValue);
    }

    private String getId(String kvsName)
    {
        return "(KvsName=" + kvsName + ")";
    }

    private class KvsInitMaps implements InitMaps
    {
        // place holder class for future init maps
    }
}
