package com.linbit.linstor.api.rest.v1;

import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.rest.v1.serializer.Json;
import com.linbit.linstor.core.apicallhandler.controller.CtrlSnapshotRestoreApiCallHandler;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.grizzly.http.server.Request;
import reactor.core.publisher.Flux;

@Path("resource-definitions/{rscName}/snapshot-restore-resource")
public class SnapshotRestoreResource
{
    private final ObjectMapper objectMapper;
    private final RequestHelper requestHelper;
    private final CtrlSnapshotRestoreApiCallHandler ctrlSnapshotRestoreApiCallHandler;

    @Inject
    SnapshotRestoreResource(
        RequestHelper requestHelperRef,
        CtrlSnapshotRestoreApiCallHandler ctrlSnapshotRestoreApiCallHandlerRef
    )
    {
        requestHelper = requestHelperRef;
        ctrlSnapshotRestoreApiCallHandler = ctrlSnapshotRestoreApiCallHandlerRef;

        objectMapper = new ObjectMapper();
    }

    @POST
    @Path("{snapName}")
    public void restoreResource(
        @Context Request request,
        @Suspended final AsyncResponse asyncResponse,
        @PathParam("rscName") String rscName,
        @PathParam("snapName") String snapName,
        String jsonData
    )
    {
        try
        {
            Json.SnapshotRestore snapRestore = objectMapper.readValue(
                jsonData,
                Json.SnapshotRestore.class
            );

            Flux<ApiCallRc> flux = ctrlSnapshotRestoreApiCallHandler.restoreSnapshot(
                snapRestore.nodes,
                rscName,
                snapName,
                snapRestore.to_resource
            ).subscriberContext(requestHelper.createContext(ApiConsts.API_RESTORE_SNAPSHOT, request));

            requestHelper.doFlux(asyncResponse, ApiCallRcConverter.mapToMonoResponse(flux));
        }
        catch (IOException ioExc)
        {
            ApiCallRcConverter.handleJsonParseException(ioExc, asyncResponse);
        }
    }
}
