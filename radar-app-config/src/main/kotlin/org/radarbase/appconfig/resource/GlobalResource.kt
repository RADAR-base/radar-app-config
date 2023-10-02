package org.radarbase.appconfig.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
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
import org.radarbase.jersey.service.AsyncCoroutineService

@Path("global")
@Singleton
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class GlobalResource(
    @Context private val configService: ConfigService,
    @Context private val clientService: ClientService,
    @Context private val asyncService: AsyncCoroutineService,
) {
    @POST
    @Path("config/{clientId}")
    @NeedsPermission(Permission.PROJECT_CREATE)
    fun updateConfig(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("clientId") clientId: String,
        config: ClientConfig,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        clientService.ensureClient(clientId)
        configService.putGlobalConfig(config, clientId)
        configService.globalConfig(clientId)
    }

    @Path("config/{clientId}")
    @GET
    fun globalConfig(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("clientId") clientId: String,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        clientService.ensureClient(clientId)
        configService.globalConfig(clientId)
    }
}
