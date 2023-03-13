package org.radarbase.appconfig.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.coroutines.runAsCoroutine

@Path("global")
@Singleton
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class GlobalResource(
    @Context private val configService: ConfigService,
    @Context private val clientService: ClientService,
) {
    @POST
    @Path("config/{clientId}")
    @NeedsPermission(Permission.PROJECT_CREATE)
    fun updateConfig(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("clientId") clientId: String,
        config: ClientConfig,
    ) = asyncResponse.runAsCoroutine {
        clientService.ensureClient(clientId)
        configService.putGlobalConfig(config, clientId)
        configService.globalConfig(clientId)
    }

    @Path("config/{clientId}")
    @GET
    fun globalConfig(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("clientId") clientId: String,
    ) = asyncResponse.runAsCoroutine {
        clientService.ensureClient(clientId)
        configService.globalConfig(clientId)
    }
}
