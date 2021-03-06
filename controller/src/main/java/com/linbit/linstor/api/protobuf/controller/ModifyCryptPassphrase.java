package com.linbit.linstor.api.protobuf.controller;

import com.linbit.linstor.api.ApiCall;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.protobuf.ApiCallAnswerer;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.apicallhandler.controller.CtrlApiCallHandler;
import com.linbit.linstor.proto.requests.MsgModCryptPassphraseOuterClass.MsgModCryptPassphrase;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

@ProtobufApiCall(
    name = ApiConsts.API_MOD_CRYPT_PASS,
    description = "Modifies the passphrase of the master key"
)
@Singleton
public class ModifyCryptPassphrase implements ApiCall
{
    private final CtrlApiCallHandler apiCallHandler;
    private final ApiCallAnswerer apiCallAnswerer;

    @Inject
    public ModifyCryptPassphrase(CtrlApiCallHandler apiCallHandlerRef, ApiCallAnswerer apiCallAnswererRef)
    {
        apiCallHandler = apiCallHandlerRef;
        apiCallAnswerer = apiCallAnswererRef;
    }

    @Override
    public void execute(InputStream msgDataIn) throws IOException
    {
        MsgModCryptPassphrase protoMsg = MsgModCryptPassphrase.parseDelimitedFrom(msgDataIn);
        apiCallAnswerer.answerApiCallRc(
            apiCallHandler.setMasterPassphrase(
                protoMsg.getNewPassphrase(),
                protoMsg.getOldPassphrase()
            )
        );
    }
}
