package com.linbit.linstor.api.protobuf.controller;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.linbit.linstor.NetInterface.NetInterfaceApi;
import com.linbit.linstor.api.ApiCall;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.protobuf.ApiCallAnswerer;
import com.linbit.linstor.api.protobuf.ProtoMapUtils;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.apicallhandler.controller.CtrlApiCallHandler;
import com.linbit.linstor.proto.requests.MsgCrtNodeOuterClass.MsgCrtNode;
import com.linbit.linstor.proto.common.NetInterfaceOuterClass;
import com.linbit.linstor.proto.common.NodeOuterClass;
import com.linbit.linstor.proto.apidata.NetInterfaceApiData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@ProtobufApiCall(
    name = ApiConsts.API_CRT_NODE,
    description = "Creates a node"
)
@Singleton
public class CreateNode implements ApiCall
{
    private final CtrlApiCallHandler apiCallHandler;
    private final ApiCallAnswerer apiCallAnswerer;

    @Inject
    public CreateNode(
        CtrlApiCallHandler apiCallHandlerRef,
        ApiCallAnswerer apiCallAnswererRef
    )
    {
        apiCallHandler = apiCallHandlerRef;
        apiCallAnswerer = apiCallAnswererRef;
    }

    @Override
    public void execute(InputStream msgDataIn)
        throws IOException
    {
        MsgCrtNode msgCreateNode = MsgCrtNode.parseDelimitedFrom(msgDataIn);
        NodeOuterClass.Node protoNode = msgCreateNode.getNode();
        ApiCallRc apiCallRc = apiCallHandler.createNode(
            // nodeUuid is ignored here
            protoNode.getName(),
            protoNode.getType(),
            extractNetIfs(protoNode.getNetInterfacesList()),
            ProtoMapUtils.asMap(protoNode.getPropsList())
        );
        apiCallAnswerer.answerApiCallRc(apiCallRc);
    }

    private List<NetInterfaceApi> extractNetIfs(List<NetInterfaceOuterClass.NetInterface> protoNetIfs)
    {
        List<NetInterfaceApi> netIfs = new ArrayList<>();
        for (NetInterfaceOuterClass.NetInterface protoNetIf : protoNetIfs)
        {
            netIfs.add(new NetInterfaceApiData(protoNetIf));
        }
        return netIfs;
    }
}
