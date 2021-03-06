package com.linbit.linstor;

import javax.inject.Inject;

import com.linbit.InvalidNameException;
import com.linbit.linstor.ResourceDefinition.TransportType;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.GenericDbBase;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;
import com.linbit.utils.Pair;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ResourceConnectionDataGenericDbDriverTest extends GenericDbBase
{
    private static final String SELECT_ALL_RES_CON_DFNS =
        " SELECT " + UUID + ", " + NODE_NAME_SRC + ", " +
                     NODE_NAME_DST + ", " + RESOURCE_NAME +
        " FROM " + TBL_RESOURCE_CONNECTIONS;

    private final ResourceName resName;
    private final Integer resPort;
    private final NodeName sourceName;
    private final NodeName targetName;

    private java.util.UUID uuid;
    private ResourceDefinitionData resDfn;
    private NodeData nodeSrc;
    private NodeData nodeDst;

    private ResourceConnectionData resCon;

    @Inject private ResourceConnectionDataGenericDbDriver driver;

    private Integer nodeIdSrc;
    private Integer nodeIdDst;

    private ResourceData resSrc;
    private ResourceData resDst;

    @SuppressWarnings("checkstyle:magicnumber")
    public ResourceConnectionDataGenericDbDriverTest() throws InvalidNameException
    {
        resName = new ResourceName("testResourceName");
        resPort = 9001;

        sourceName = new NodeName("testNodeSource");
        targetName = new NodeName("testNodeTarget");
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @Before
    public void setUp() throws Exception
    {
        super.setUpAndEnterScope();
        assertEquals(
            TBL_RESOURCE_CONNECTIONS + " table's column count has changed. Update tests accordingly!",
            6,
            TBL_COL_COUNT_RESOURCE_CONNECTIONS
        );

        uuid = randomUUID();

        resDfn = resourceDefinitionDataFactory.create(
            SYS_CTX,
            resName,
            null,
            resPort,
            null,
            "secret",
            TransportType.IP,
            Arrays.asList(DeviceLayerKind.DRBD, DeviceLayerKind.STORAGE)
        );
        rscDfnMap.put(resDfn.getName(), resDfn);
        nodeSrc = nodeDataFactory.create(SYS_CTX, sourceName, null, null);
        nodeDst = nodeDataFactory.create(SYS_CTX, targetName, null, null);

        nodeIdSrc = 13;
        nodeIdDst = 14;

        resSrc = resourceDataFactory.create(SYS_CTX, resDfn, nodeSrc, nodeIdSrc, null, Collections.emptyList());
        resDst = resourceDataFactory.create(SYS_CTX, resDfn, nodeDst, nodeIdDst, null, Collections.emptyList());

        resCon = new ResourceConnectionData(
            uuid,
            resSrc,
            resDst,
            null,
            tcpPortPoolMock,
            driver,
            propsContainerFactory,
            transObjFactory,
            transMgrProvider,
            0
        );
    }

    @Test
    public void testPersist() throws Exception
    {
        driver.create(resCon);
        commit();

        checkDbPersist(true);
    }

    @Test
    public void testPersistGetInstance() throws Exception
    {
        resourceConnectionDataFactory.create(SYS_CTX, resSrc, resDst, null);
        commit();

        checkDbPersist(false);
    }

    @Test
    public void testLoadAll() throws Exception
    {
        driver.create(resCon);

        Map<Pair<NodeName, ResourceName>, ResourceData> rscmap = new HashMap<>();
        rscmap.put(new Pair<NodeName, ResourceName>(sourceName, resName), resSrc);
        rscmap.put(new Pair<NodeName, ResourceName>(targetName, resName), resDst);
        List<ResourceConnectionData> cons = driver.loadAll(rscmap);

        assertNotNull(cons);

        assertEquals(1, cons.size());

        ResourceConnection loadedConDfn = cons.get(0);
        assertNotNull(loadedConDfn);

        checkLoadedConDfn(loadedConDfn, true);
    }

    @Test
    public void testLoadGetInstance() throws Exception
    {
        driver.create(resCon);
        resSrc.setResourceConnection(SYS_CTX, resCon);
        resDst.setResourceConnection(SYS_CTX, resCon);

        ResourceConnectionData loadedConDfn = ResourceConnectionData.get(
            SYS_CTX,
            resSrc,
            resDst
        );

        checkLoadedConDfn(loadedConDfn, true);
    }

    @Test
    public void testCache() throws Exception
    {
        ResourceConnectionData storedInstance = resourceConnectionDataFactory.create(
            SYS_CTX,
            resSrc,
            resDst,
            null
        );

        // no clear-cache

        assertEquals(storedInstance, ResourceConnectionData.get(
            SYS_CTX,
            resSrc,
            resDst
        ));
    }

    @Test
    public void testDelete() throws Exception
    {
        driver.create(resCon);
        commit();

        PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_RES_CON_DFNS);
        ResultSet resultSet = stmt.executeQuery();

        assertTrue(resultSet.next());
        assertFalse(resultSet.next());
        resultSet.close();

        driver.delete(resCon);

        resultSet = stmt.executeQuery();

        assertFalse(resultSet.next());
        resultSet.close();

        stmt.close();
    }

    private void checkDbPersist(boolean checkUuid) throws SQLException
    {
        PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL_RES_CON_DFNS);
        ResultSet resultSet = stmt.executeQuery();

        assertTrue(resultSet.next());
        if (checkUuid)
        {
            assertEquals(uuid, java.util.UUID.fromString(resultSet.getString(UUID)));
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
        Resource sourceResource = loadedConDfn.getSourceResource(SYS_CTX);
        Resource targetResource = loadedConDfn.getTargetResource(SYS_CTX);

        assertEquals(resName, sourceResource.getDefinition().getName());
        assertEquals(sourceName, sourceResource.getAssignedNode().getName());
        assertEquals(targetName, targetResource.getAssignedNode().getName());
        assertEquals(sourceResource.getDefinition().getName(), targetResource.getDefinition().getName());
    }

    @Test (expected = LinStorDataAlreadyExistsException.class)
    public void testAlreadyExists() throws Exception
    {
        driver.create(resCon);
        resSrc.setResourceConnection(SYS_CTX, resCon);
        resDst.setResourceConnection(SYS_CTX, resCon);

        resourceConnectionDataFactory.create(SYS_CTX, resSrc, resDst, null);
    }
}
