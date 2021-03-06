package com.linbit.linstor.api.protobuf.satellite;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.linbit.linstor.InternalApiConsts;
import com.linbit.linstor.api.ApiCall;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.apicallhandler.satellite.StltApiCallHandler;
import com.linbit.linstor.proto.javainternal.s2c.MsgIntPrimaryOuterClass.MsgIntPrimary;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 *
 * @author rpeinthor
 */
@ProtobufApiCall(
    name = InternalApiConsts.API_PRIMARY_RSC,
    description = "Controller notifies the satellite that one of his resources should become primary"
)
@Singleton
public class PrimaryResource implements ApiCall
{
    private final StltApiCallHandler apiCallHandler;

    @Inject
    public PrimaryResource(StltApiCallHandler apiCallHandlerRef)
    {
        apiCallHandler = apiCallHandlerRef;
    }

    @Override
    public void execute(InputStream msgDataIn)
        throws IOException
    {
        MsgIntPrimary msgReqPrimary = MsgIntPrimary.parseDelimitedFrom(msgDataIn);
        apiCallHandler.handlePrimaryResource(
            msgReqPrimary.getRscName(),
            UUID.fromString(msgReqPrimary.getRscUuid())
        );
    }
}
