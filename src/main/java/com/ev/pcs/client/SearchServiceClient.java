package com.ev.pcs.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;


@RegisterRestClient
@RestClient
@Path("/api/search")
public interface SearchServiceClient {

    @GET
    @Path("/slots/{slotId}/availability")
    Uni<Response> checkAvailability(@PathParam("slotId") String slotId,
                                   @QueryParam("startTime") String startTime,
                                   @QueryParam("endTime") String endTime);
}