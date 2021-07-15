package org.radarbase.appconfig.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import org.radarbase.appconfig.domain.ClientProtocol
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
    fun getProtocol(
        @PathParam("protocolId") protocolId: Long,
        @Context auth: Auth,
    ): ClientProtocol {
        return protocolService.getProtocol(protocolId, auth)
    }
}
