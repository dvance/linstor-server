package com.linbit.linstor.api.protobuf.controller;

import com.linbit.linstor.api.ApiCallReactive;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.apicallhandler.ResponseSerializer;
import com.linbit.linstor.core.apicallhandler.controller.CtrlRscToggleDiskApiCallHandler;
import com.linbit.linstor.proto.requests.MsgToggleDiskOuterClass.MsgToggleDisk;
import reactor.core.publisher.Flux;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@ProtobufApiCall(
    name = ApiConsts.API_TOGGLE_DISK,
    description = "Toggles a resource between diskless and having a disk"
)
@Singleton
public class ToggleDisk implements ApiCallReactive
{
    private final CtrlRscToggleDiskApiCallHandler ctrlRscToggleDiskApiCallHandler;
    private final ResponseSerializer responseSerializer;

    @Inject
    public ToggleDisk(
        CtrlRscToggleDiskApiCallHandler ctrlRscToggleDiskApiCallHandlerRef,
        ResponseSerializer responseSerializerRef
    )
    {
        ctrlRscToggleDiskApiCallHandler = ctrlRscToggleDiskApiCallHandlerRef;
        responseSerializer = responseSerializerRef;
    }

    @Override
    public Flux<byte[]> executeReactive(InputStream msgDataIn)
        throws IOException
    {
        MsgToggleDisk msgToggleDisk = MsgToggleDisk.parseDelimitedFrom(msgDataIn);
        UUID rscUuid = null;
        if (msgToggleDisk.hasRscUuid())
        {
            rscUuid = UUID.fromString(msgToggleDisk.getRscUuid());
        }
        String nodeName = msgToggleDisk.getNodeName();
        String rscName = msgToggleDisk.getRscName();
        String storPoolName = msgToggleDisk.getStorPoolName();
        String migrateFromNodeName = msgToggleDisk.getMigrateFrom();
        boolean removeDisk = msgToggleDisk.getDiskless();

        return ctrlRscToggleDiskApiCallHandler
            .resourceToggleDisk(
                nodeName,
                rscName,
                storPoolName,
                migrateFromNodeName,
                removeDisk
            )
            .transform(responseSerializer::transform);
    }

}
