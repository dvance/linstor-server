package com.linbit.linstor.api.protobuf.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.linbit.linstor.InternalApiConsts;
import com.linbit.linstor.api.ApiCall;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.apicallhandler.controller.CtrlApiCallHandler;
import com.linbit.linstor.core.apicallhandler.controller.internal.StorPoolInternalCallHandler;
import com.linbit.linstor.proto.javainternal.IntObjectIdOuterClass.IntObjectId;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@ProtobufApiCall(
    name = InternalApiConsts.API_REQUEST_STOR_POOL,
    description = "Called by the satellite to request storage pool update data",
    transactional = false
)
@Singleton
public class IntRequestStorPool implements ApiCall
{
    private final StorPoolInternalCallHandler storPoolInternalCallHandler;

    @Inject
    public IntRequestStorPool(StorPoolInternalCallHandler apiCallHandlerRef)
    {
        storPoolInternalCallHandler = apiCallHandlerRef;
    }

    @Override
    public void execute(InputStream msgDataIn)
        throws IOException
    {
        IntObjectId storPoolId = IntObjectId.parseDelimitedFrom(msgDataIn);
        UUID storPoolUuid = UUID.fromString(storPoolId.getUuid());
        String storPoolName = storPoolId.getName();

        storPoolInternalCallHandler.handleStorPoolRequest(storPoolUuid, storPoolName);
    }
}
