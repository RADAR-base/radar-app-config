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

private const val PUBLIC_CONFIG_SERVICE: String = "public_config_service"

@Path("/public")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class PublicResource(
    @Context private val configService: ConfigService,
    @Context private val clientService: ClientService,
    @Context private val asyncService: AsyncCoroutineService,
) {
    @Path("config")
    @GET
    fun globalConfig(
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        clientService.ensureClient(PUBLIC_CONFIG_SERVICE)
        configService.globalConfig(PUBLIC_CONFIG_SERVICE)
    }


}
