package org.radarbase.appconfig.resource

import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarcns.auth.authorization.Permission
import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType

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
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.CREATE)
    fun updateConfig(
        @PathParam("clientId") clientId: String,
        config: ClientConfig,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        configService.putGlobalConfig(config, clientId)
        return configService.globalConfig(clientId)
    }

    @Path("config/{clientId}")
    @GET
    fun globalConfig(
        @PathParam("clientId") clientId: String,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        return configService.globalConfig(clientId)
    }
}
