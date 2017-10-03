package com.linbit.drbdmanage;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.linbit.TransactionMgr;
import com.linbit.drbdmanage.Node.NodeType;
import com.linbit.drbdmanage.propscon.InvalidKeyException;
import com.linbit.drbdmanage.propscon.InvalidValueException;
import com.linbit.drbdmanage.propscon.Props;
import com.linbit.drbdmanage.security.AccessDeniedException;
import com.linbit.drbdmanage.security.DerbyBase;

public class ConnectionPropsTest extends DerbyBase
{
    private NodeName nodeName1;
    private NodeName nodeName2;
    private ResourceName resName;
    private NodeId nodeId1;
    private NodeId nodeId2;
    private VolumeNumber volNr;
    private MinorNumber minor;
    private int volSize;
    private String blockDev1;
    private String metaDisk1;
    private String blockDev2;
    private String metaDisk2;

    private TransactionMgr transMgr;

    private NodeData node1;
    private NodeData node2;
    private ResourceDefinitionData resDfn;
    private ResourceData res1;
    private ResourceData res2;
    private VolumeDefinitionData volDfn;
    private VolumeData vol1;
    private VolumeData vol2;

    private NodeConnectionData nodeCon;
    private ResourceConnectionData resCon;
    private VolumeConnectionData volCon;

    private Props nodeConProps;
    private Props resConProps;
    private Props volConProps;

    private ConnectionProps conProps;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        nodeName1 = new NodeName("Node1");
        nodeName2 = new NodeName("Node2");
        resName = new ResourceName("ResName");
        nodeId1 = new NodeId(1);
        nodeId2 = new NodeId(2);
        volNr = new VolumeNumber(13);
        minor = new MinorNumber(12);
        volSize = 9001;
        blockDev1 = "/dev/vol1/block";
        metaDisk1= "/dev/vol1/meta";
        blockDev2 = "/dev/vol2/block";
        metaDisk2 = "/dev/vol2/meta";

        transMgr = new TransactionMgr(getConnection());

        node1 = NodeData.getInstance(sysCtx, nodeName1, NodeType.CONTROLLER, null, transMgr, true);
        node2 = NodeData.getInstance(sysCtx, nodeName2, NodeType.CONTROLLER, null, transMgr, true);

        resDfn = ResourceDefinitionData.getInstance(sysCtx, resName, null, transMgr, true);

        res1 = ResourceData.getInstance(sysCtx, resDfn, node1, nodeId1, null, transMgr, true);
        res2 = ResourceData.getInstance(sysCtx, resDfn, node2, nodeId2, null, transMgr, true);

        volDfn = VolumeDefinitionData.getInstance(sysCtx, resDfn, volNr, minor, volSize, null, transMgr, true);

        vol1 = VolumeData.getInstance(sysCtx, res1, volDfn, blockDev1, metaDisk1, null, transMgr, true);
        vol2 = VolumeData.getInstance(sysCtx, res1, volDfn, blockDev2, metaDisk2, null, transMgr, true);

        nodeCon = NodeConnectionData.getInstance(sysCtx, node1, node2, transMgr, true);
        resCon = ResourceConnectionData.getInstance(sysCtx, res1, res2, transMgr, true);
        volCon = VolumeConnectionData.getInstance(sysCtx, vol1, vol2, transMgr, true);

        nodeConProps = nodeCon.getProps(sysCtx);
        resConProps = resCon.getProps(sysCtx);
        volConProps = volCon.getProps(sysCtx);

        conProps = new ConnectionProps(sysCtx, nodeCon, resCon, volCon);
    }

    @Test
    public void test() throws InvalidKeyException, AccessDeniedException, InvalidValueException, SQLException
    {
        String testKey = "testKey";
        String testValue1 = "testValue1";
        String testValue2 = "testValue2";
        String testValue3 = "testValue3";
        String testValue4 = "testValue4";
        assertNull(conProps.getProp(testKey));

        volConProps.setProp(testKey, testValue1);
        assertEquals(testValue1, conProps.getProp(testKey));

        resConProps.setProp(testKey, testValue2);
        assertEquals(testValue1, conProps.getProp(testKey));

        nodeConProps.setProp(testKey, testValue3);
        assertEquals(testValue1, conProps.getProp(testKey));

        volConProps.removeProp(testKey);
        assertEquals(testValue2, conProps.getProp(testKey));

        resConProps.removeProp(testKey);
        assertEquals(testValue3, conProps.getProp(testKey));

        volConProps.setProp(testKey, testValue4);
        assertEquals(testValue4, conProps.getProp(testKey));
    }

}