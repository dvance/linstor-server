package com.linbit.linstor.api.rest.v1;

import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.rest.v1.serializer.Json;
import com.linbit.linstor.core.apicallhandler.controller.CtrlApiCallHandler;

import javax.inject.Inject;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.grizzly.http.server.Request;

@Path("encryption")
public class Encryption
{
    private ObjectMapper objectMapper;
    private final RequestHelper requestHelper;
    private CtrlApiCallHandler ctrlApiCallHandler;

    @Inject
    public Encryption(
        RequestHelper requestHelperRef,
        CtrlApiCallHandler ctrlApiCallHandlerRef
    )
    {
        requestHelper = requestHelperRef;
        ctrlApiCallHandler = ctrlApiCallHandlerRef;

        objectMapper = new ObjectMapper();
    }

    @POST
    @Path("passphrase")
    public Response createPassphrase(
        @Context Request request,
        String jsonData
    )
    {
        return requestHelper.doInScope(ApiConsts.API_CRT_CRYPT_PASS, request, () ->
        {
            Json.PassPhraseCreate passPhraseCreate = objectMapper.readValue(jsonData, Json.PassPhraseCreate.class);

            ApiCallRc apiCallRc = ctrlApiCallHandler.setMasterPassphrase(
                passPhraseCreate.new_passphrase,
                null
            );

            return ApiCallRcConverter.toResponse(apiCallRc, Response.Status.CREATED);
        }, true);
    }

    @PUT
    @Path("passphrase")
    public Response modifyPassphrase(
        @Context Request request,
        String jsonData
    )
    {
        return requestHelper.doInScope(ApiConsts.API_MOD_CRYPT_PASS, request, () ->
        {
            Json.PassPhraseCreate passPhraseCreate = objectMapper.readValue(jsonData, Json.PassPhraseCreate.class);

            ApiCallRc apiCallRc = ctrlApiCallHandler.setMasterPassphrase(
                passPhraseCreate.new_passphrase,
                passPhraseCreate.old_passphrase
            );

            return ApiCallRcConverter.toResponse(apiCallRc, Response.Status.OK);
        }, true);
    }

    @PATCH
    @Path("passphrase")
    public Response enterPassphrase(
        @Context Request request,
        String jsonData
    )
    {
        return requestHelper.doInScope(ApiConsts.API_MOD_CRYPT_PASS, request, () ->
        {
            String passPhrase = objectMapper.readValue(jsonData, String.class);

            ApiCallRc apiCallRc = ctrlApiCallHandler.enterPassphrase(passPhrase);

            return ApiCallRcConverter.toResponse(apiCallRc, Response.Status.OK);
        }, true);
    }
}
