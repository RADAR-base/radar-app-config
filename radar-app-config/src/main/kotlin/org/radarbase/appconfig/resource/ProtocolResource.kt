package org.radarbase.appconfig.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import org.radarbase.appconfig.api.ClientProtocol
import org.radarbase.appconfig.service.ProtocolService
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.Authenticated

@Path("protocols")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
@Authenticated
class ProtocolResource(
    @Context val protocolService: ProtocolService,
) {
    @Path("protocol/{protocolId}")
    @GET
    @Operation(
        description = "Get a protocol by ID.",
        responses = [ApiResponse(description = "Single protocol with client configuration", responseCode = "200"), ],
    )
    fun getProtocol(
        @PathParam("protocolId") protocolId: Long,
        @Context auth: Auth,
    ): ClientProtocol {
        return protocolService.getProtocol(protocolId, auth)
    }
}
