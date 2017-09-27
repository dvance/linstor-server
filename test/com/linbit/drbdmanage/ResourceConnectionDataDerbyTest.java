package com.linbit.drbdmanage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.Test;

import com.linbit.InvalidNameException;
import com.linbit.TransactionMgr;
import com.linbit.drbdmanage.core.DrbdManage;
import com.linbit.drbdmanage.security.AccessDeniedException;
import com.linbit.drbdmanage.security.DerbyBase;
import com.linbit.utils.UuidUtils;

public class ResourceConnectionDataDerbyTest extends DerbyBase
{
    private static final String SELECT_ALL_RES_CON_DFNS =
        " SELECT " + UUID + ", " + NODE_NAME_SRC + ", " +
                     NODE_NAME_DST + ", " + RESOURCE_NAME +
        " FROM " + TBL_RESOURCE_CONNECTIONS;

    private final ResourceName resName;
    private final NodeName sourceName;
    private final NodeName targetName;

    private TransactionMgr transMgr;

    private java.util.UUID uuid;
    private ResourceDefinitionData resDfn;
    private NodeData nodeSrc;
    private NodeData nodeDst;

    private ResourceConnectionData resCon;

    private ResourceConnectionDataDerbyDriver driver;

    private NodeId nodeIdSrc;
    private NodeId nodeIdDst;

    private ResourceData resSrc;
    private ResourceData resDst;

    public ResourceConnectionDataDerbyTest() throws InvalidNameException
    {
        resName = new ResourceName("testResourceName");
        sourceName = new NodeName("testNodeSource");
        targetName = new NodeName("testNodeTarget");
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        assertEquals(TBL_RESOURCE_CONNECTIONS + " table's column count has changed. Update tests accordingly!", 4, TBL_COL_COUNT_RESOURCE_CONNECTIONS);

        transMgr = new TransactionMgr(getConnection());

        uuid = randomUUID();

        resDfn = ResourceDefinitionData.getInstance(sysCtx, resName, null, transMgr, true);
        nodeSrc = NodeData.getInstance(sysCtx, sourceName, null, null, transMgr, true);
        nodeDst = NodeData.getInstance(sysCtx, targetName, null, null, transMgr, true);

        nodeIdSrc = new NodeId(13);
        nodeIdDst = new NodeId(14);

        resSrc = ResourceData.getInstance(sysCtx, resDfn, nodeSrc, nodeIdSrc, null, transMgr, true);
        resDst = ResourceData.getInstance(sysCtx, resDfn, nodeDst, nodeIdDst, null, transMgr, true);

        resCon = new ResourceConnectionData(uuid, resSrc, resDst, transMgr);
        driver = (ResourceConnectionDataDerbyDriver) DrbdManage.getResourceConnectionDatabaseDriver();
    }

    @Test
    public void testPersist() throws Exception
    {
        driver.create(resCon, transMgr);

        checkDbPersist(true);
    }

    @Test
    public void testPersistGetInstance() throws Exception
    {
        ResourceConnectionData.getInstance(sysCtx, resSrc, resDst, transMgr, true);

        checkDbPersist(false);
    }

    @Test
    public void testLoad() throws Exception
    {
        driver.create(resCon, transMgr);

        ResourceConnectionData loadedConDfn = driver.load(resSrc , resDst, transMgr);

        checkLoadedConDfn(loadedConDfn, true);
    }

    @Test
    public void testLoadAll() throws Exception
    {
        driver.create(resCon, transMgr);

        List<ResourceConnectionData> cons = driver.loadAllByResource(resSrc, transMgr);

        assertNotNull(cons);

        assertEquals(1, cons.size());

        ResourceConnection loadedConDfn = cons.get(0);
        assertNotNull(loadedConDfn);

        checkLoadedConDfn(loadedConDfn, true);
    }

    @Test
    public void testLoadGetInstance() throws Exception
    {
        driver.create(resCon, transMgr);

        ResourceConnectionData loadedConDfn = ResourceConnectionData.getInstance(
            sysCtx,
            resSrc,
            resDst,
            transMgr,
            false
        );

        checkLoadedConDfn(loadedConDfn, true);
    }

    @Test
    public void testCache() throws Exception
    {
        ResourceConnectionData storedInstance = ResourceConnectionData.getInstance(
            sysCtx,
            resSrc,
            resDst,
            transMgr,
            true
        );

        // no clear-cache

        assertEquals(storedInstance, driver.load(resSrc, resDst, transMgr));
    }

    @Test
    public void testDelete() throws Exception
    {
        driver.create(resCon, transMgr);

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RES_CON_DFNS);
        ResultSet resultSet = stmt.executeQuery();

        assertTrue(resultSet.next());
        assertFalse(resultSet.next());
        resultSet.close();

        driver.delete(resCon, transMgr);

        resultSet = stmt.executeQuery();

        assertFalse(resultSet.next());
        resultSet.close();

        stmt.close();
    }

    @Test
    public void testSatelliteCreate() throws Exception
    {
        ResourceConnectionData satelliteConDfn = ResourceConnectionData.getInstance(
            sysCtx,
            resSrc,
            resDst,
            null,
            true
        );

        checkLoadedConDfn(satelliteConDfn, false);

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RES_CON_DFNS);
        ResultSet resultSet = stmt.executeQuery();

        assertFalse(resultSet.next());
        resultSet.close();
        stmt.close();
    }

    @Test
    public void testSatelliteNoCreate() throws Exception
    {
        ResourceConnectionData satelliteConDfn = ResourceConnectionData.getInstance(
            sysCtx,
            resSrc,
            resDst,
            null,
            false
        );

        assertNull(satelliteConDfn);

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RES_CON_DFNS);
        ResultSet resultSet = stmt.executeQuery();

        assertFalse(resultSet.next());
        resultSet.close();
        stmt.close();
    }

    private void checkDbPersist(boolean checkUuid) throws SQLException
    {
        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RES_CON_DFNS);
        ResultSet resultSet = stmt.executeQuery();

        assertTrue(resultSet.next());
        if (checkUuid)
        {
            assertEquals(uuid, UuidUtils.asUuid(resultSet.getBytes(UUID)));
        }
        assertEquals(resName.value, resultSet.getString(RESOURCE_NAME));
        assertEquals(sourceName.value, resultSet.getString(NODE_NAME_SRC));
        assertEquals(targetName.value, resultSet.getString(NODE_NAME_DST));

        assertFalse(resultSet.next());

        resultSet.close();
        stmt.close();
    }

    private void checkLoadedConDfn(ResourceConnection loadedConDfn, boolean checkUuid) throws AccessDeniedException
    {
        assertNotNull(loadedConDfn);
        if (checkUuid)
        {
            assertEquals(uuid, loadedConDfn.getUuid());
        }
        Resource sourceResource = loadedConDfn.getSourceResource(sysCtx);
        Resource targetResource = loadedConDfn.getTargetResource(sysCtx);

        assertEquals(resName, sourceResource.getDefinition().getName());
        assertEquals(sourceName, sourceResource.getAssignedNode().getName());
        assertEquals(targetName, targetResource.getAssignedNode().getName());
        assertEquals(sourceResource.getDefinition().getName(), targetResource.getDefinition().getName());
    }
}