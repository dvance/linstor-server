package com.linbit.linstor.api.protobuf.controller;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.linbit.linstor.api.ApiCall;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.protobuf.ApiCallAnswerer;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.apicallhandler.controller.CtrlApiCallHandler;
import com.linbit.linstor.proto.requests.MsgDelStorPoolDfnOuterClass.MsgDelStorPoolDfn;

import java.io.IOException;
import java.io.InputStream;

@ProtobufApiCall(
    name = ApiConsts.API_DEL_STOR_POOL_DFN,
    description = "Deletes a storage pool definition"
)
@Singleton
public class DeleteStorPoolDfn implements ApiCall
{
    private final CtrlApiCallHandler apiCallHandler;
    private final ApiCallAnswerer apiCallAnswerer;

    @Inject
    public DeleteStorPoolDfn(CtrlApiCallHandler apiCallHandlerRef, ApiCallAnswerer apiCallAnswererRef)
    {
        apiCallHandler = apiCallHandlerRef;
        apiCallAnswerer = apiCallAnswererRef;
    }

    @Override
    public void execute(InputStream msgDataIn)
        throws IOException
    {
        MsgDelStorPoolDfn msgDeleteStorPoolDfn = MsgDelStorPoolDfn.parseDelimitedFrom(msgDataIn);
        ApiCallRc apiCallRc = apiCallHandler.deleteStoragePoolDefinition(
            msgDeleteStorPoolDfn.getStorPoolName()
        );

        apiCallAnswerer.answerApiCallRc(apiCallRc);
    }
}
