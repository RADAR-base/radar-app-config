package org.radarbase.appconfig.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.NonResolvingConfigService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope

/*
 * Provides read-only access for external services without scope evaluation.
 */
@Path("service")
@Singleton
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ServiceResource(
    @Context private val configService: NonResolvingConfigService,
    @Context private val clientService: ClientService,
    @Context private val asyncService: AsyncCoroutineService,
) {

    @Path("config/{clientId}/search")
    @GET
    // TODO add authorization annotation!!!!
    fun getGlobalConfig(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("clientId") clientId: String,
        @QueryParam("scope") scopes: List<Scope>?,
        @QueryParam("name") name: String?,
        @QueryParam("version") version: Int?,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        clientService.ensureClient(clientId)
        configService.getConfig(
            clientId = clientId,
            scopes = scopes,
            name = name?.let { QualifiedId(it) },
            version = version,
        )
    }
}
