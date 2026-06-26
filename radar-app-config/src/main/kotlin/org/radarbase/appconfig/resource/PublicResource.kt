package org.radarbase.appconfig.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.GlobalConfigService
import org.radarbase.jersey.service.AsyncCoroutineService

private const val PUBLIC_CONFIG_SERVICE: String = "public_config_service"

@Path("/public")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class PublicResource(
    @Context private val globalConfigService: GlobalConfigService,
    @Context private val clientService: ClientService,
    @Context private val asyncService: AsyncCoroutineService,
) {
    @Path("config")
    @GET
    fun getPublicConfig(
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        clientService.ensureClient(PUBLIC_CONFIG_SERVICE)
        globalConfigService.getGlobalConfig(PUBLIC_CONFIG_SERVICE)
    }
}
