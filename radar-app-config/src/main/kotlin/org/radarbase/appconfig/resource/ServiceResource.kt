package org.radarbase.appconfig.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.GlobalConfigService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.service.AsyncCoroutineService

@Path("service")
@Singleton
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
/*
 * Provides read-only access for external services without scope evaluation.
 */
class ServiceResource(
    @Context private val globalConfigService: GlobalConfigService,
    @Context private val clientService: ClientService,
    @Context private val asyncService: AsyncCoroutineService,
) {

    @Path("config/{clientId}")
    @GET
    fun getGlobalConfig(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("clientId") clientId: String,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        clientService.ensureClient(clientId)
        globalConfigService.getGlobalConfig(clientId)
    }

    // return the most recent config of client clientId with name 'name'
    @Path("config/{clientId}/names/{name}")
    @GET
    fun getGlobalConfigByName(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("clientId") clientId: String,
        @PathParam("name") name: String,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        clientService.ensureClient(clientId)
        globalConfigService.getGlobalConfigByName(clientId, name)
    }

    // return the all versions of the config of client clientId with name name
    @Path("config/{clientId}/names/{name}/versions")
    @GET
    fun getGlobalConfigByNameAndAllVersions(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("clientId") clientId: String,
        @PathParam("name") name: String,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        clientService.ensureClient(clientId)
        globalConfigService.getGlobalConfigByNameAndAllVersions(clientId, name)
    }

    // return the version version of the config of client clientId with name name
    @Path("config/{clientId}/names/{name}/versions/{version}")
    @GET
    fun getGlobalConfigByNameAndVersion(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("clientId") clientId: String,
        @PathParam("name") name: String,
        @PathParam("version") version: Int,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        clientService.ensureClient(clientId)
        globalConfigService.getGlobalConfigByNameAndVersion(clientId, name, version)
    }
}
