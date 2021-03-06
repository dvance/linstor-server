package com.linbit.linstor.api.protobuf.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.linbit.linstor.api.ApiCall;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.protobuf.ApiCallAnswerer;
import com.linbit.linstor.api.protobuf.ProtoMapUtils;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.apicallhandler.controller.CtrlApiCallHandler;
import com.linbit.linstor.proto.requests.MsgCrtStorPoolDfnOuterClass.MsgCrtStorPoolDfn;
import com.linbit.linstor.proto.common.StorPoolDfnOuterClass.StorPoolDfn;

@ProtobufApiCall(
    name = ApiConsts.API_CRT_STOR_POOL_DFN,
    description = "Creates a storage pool definition"
)
@Singleton
public class CreateStorPoolDfn implements ApiCall
{
    private final CtrlApiCallHandler apiCallHandler;
    private final ApiCallAnswerer apiCallAnswerer;

    @Inject
    public CreateStorPoolDfn(CtrlApiCallHandler apiCallHandlerRef, ApiCallAnswerer apiCallAnswererRef)
    {
        apiCallHandler = apiCallHandlerRef;
        apiCallAnswerer = apiCallAnswererRef;
    }

    @Override
    public void execute(InputStream msgDataIn)
        throws IOException
    {
        MsgCrtStorPoolDfn msgCreateStorPoolDfn = MsgCrtStorPoolDfn.parseDelimitedFrom(msgDataIn);
        StorPoolDfn storPoolDfn = msgCreateStorPoolDfn.getStorPoolDfn();

        ApiCallRc apiCallRc = apiCallHandler.createStoragePoolDefinition(
            storPoolDfn.getStorPoolName(),
            ProtoMapUtils.asMap(storPoolDfn.getPropsList())
        );
        apiCallAnswerer.answerApiCallRc(apiCallRc);
    }
}
