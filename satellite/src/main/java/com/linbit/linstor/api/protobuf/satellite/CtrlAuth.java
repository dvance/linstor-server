package com.linbit.linstor.api.protobuf.satellite;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.linbit.linstor.InternalApiConsts;
import com.linbit.linstor.api.ApiCall;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.interfaces.serializer.CommonSerializer;
import com.linbit.linstor.api.protobuf.ApiCallAnswerer;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.LinStor;
import com.linbit.linstor.core.UpdateMonitor;
import com.linbit.linstor.core.apicallhandler.satellite.StltApiCallHandler;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.proto.javainternal.c2s.MsgIntAuthOuterClass.MsgIntAuth;
import com.linbit.linstor.proto.javainternal.s2c.MsgIntAuthSuccessOuterClass;
import com.linbit.linstor.proto.javainternal.s2c.MsgIntAuthSuccessOuterClass.MsgIntAuthSuccess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@ProtobufApiCall(
    name = InternalApiConsts.API_AUTH,
    description = "Called by the controller to authenticate the controller to the satellite",
    requiresAuth = false
)
@Singleton
public class CtrlAuth implements ApiCall
{
    private final StltApiCallHandler apiCallHandler;
    private final ApiCallAnswerer apiCallAnswerer;
    private final CommonSerializer commonSerializer;
    private final UpdateMonitor updateMonitor;
    private final Provider<Peer> controllerPeerProvider;

    @Inject
    public CtrlAuth(
        StltApiCallHandler apiCallHandlerRef,
        ApiCallAnswerer apiCallAnswererRef,
        CommonSerializer commonSerializerRef,
        UpdateMonitor updateMonitorRef,
        Provider<Peer> controllerPeerProviderRef
    )
    {
        apiCallHandler = apiCallHandlerRef;
        apiCallAnswerer = apiCallAnswererRef;
        commonSerializer = commonSerializerRef;
        updateMonitor = updateMonitorRef;
        controllerPeerProvider = controllerPeerProviderRef;
    }

    @Override
    public void execute(InputStream msgDataIn)
        throws IOException
    {
        // TODO: implement authentication
        MsgIntAuth auth = MsgIntAuth.parseDelimitedFrom(msgDataIn);
        String nodeName = auth.getNodeName();
        UUID nodeUuid = UUID.fromString(auth.getNodeUuid());

        Peer controllerPeer = controllerPeerProvider.get();
        ApiCallRcImpl apiCallRc = apiCallHandler.authenticate(nodeUuid, nodeName, controllerPeer);

        if (apiCallRc == null)
        {
            // all ok, send the new fullSyncId with the AUTH_ACCEPT msg
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MsgIntAuthSuccessOuterClass.MsgIntAuthSuccess.Builder builder = MsgIntAuthSuccess.newBuilder();
            builder.setExpectedFullSyncId(updateMonitor.getNextFullSyncId());
            int[] stltVersion = LinStor.VERSION_INFO_PROVIDER.getSemanticVersion();
            builder.setVersionMajor(stltVersion[0]);
            builder.setVersionMinor(stltVersion[1]);
            builder.setVersionPatch(stltVersion[2]);

            builder.build().writeDelimitedTo(baos);

            controllerPeer.sendMessage(
                apiCallAnswerer.prepareOnewayMessage(
                    baos.toByteArray(),
                    InternalApiConsts.API_AUTH_ACCEPT
                )
            );
        }
        else
        {
            // whatever happened should be in the apiCallRc
            controllerPeer.sendMessage(
                commonSerializer.onewayBuilder(InternalApiConsts.API_AUTH_ERROR)
                    .authError(apiCallRc)
                    .build()
            );
        }
    }
}
