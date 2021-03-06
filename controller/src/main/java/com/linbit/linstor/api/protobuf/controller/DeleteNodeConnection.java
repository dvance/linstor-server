package com.linbit.linstor.api.protobuf.controller;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.linbit.linstor.api.ApiCall;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.protobuf.ApiCallAnswerer;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.apicallhandler.controller.CtrlApiCallHandler;
import com.linbit.linstor.proto.requests.MsgDelNodeConnOuterClass.MsgDelNodeConn;

import java.io.IOException;
import java.io.InputStream;

@ProtobufApiCall(
    name = ApiConsts.API_DEL_NODE_CONN,
    description = "Deletes node connection options"
)
@Singleton
public class DeleteNodeConnection implements ApiCall
{
    private final CtrlApiCallHandler apiCallHandler;
    private final ApiCallAnswerer apiCallAnswerer;

    @Inject
    public DeleteNodeConnection(CtrlApiCallHandler apiCallHandlerRef, ApiCallAnswerer apiCallAnswererRef)
    {
        apiCallHandler = apiCallHandlerRef;
        apiCallAnswerer = apiCallAnswererRef;
    }

    @Override
    public void execute(InputStream msgDataIn)
        throws IOException
    {
        MsgDelNodeConn msgDeleteNodeConn = MsgDelNodeConn.parseDelimitedFrom(msgDataIn);
        ApiCallRc apiCallRc = apiCallHandler.deleteNodeConnection(
            msgDeleteNodeConn.getNodeName1(),
            msgDeleteNodeConn.getNodeName2()
        );
        apiCallAnswerer.answerApiCallRc(apiCallRc);
    }
}
