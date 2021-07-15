package org.radarbase.appconfig.resource

import com.fasterxml.jackson.databind.JsonNode
import org.radarbase.auth.authorization.Permission
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.radarbase.appconfig.domain.ClientProtocol

import org.radarbase.appconfig.service.ProtocolService
import org.radarbase.appconfig.service.ProtocolService.Companion.toResponse
import org.radarbase.jersey.cache.Cache

@Path("global")
@Singleton
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class GlobalResource(
    @Context private val configService: ConfigService,
    @Context private val clientService: ClientService,
    @Context private val protocolService: ProtocolService,
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

    @Path("protocol/{clientId}")
    @GET
    @Cache(maxAge = 300, isPrivate = true)
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ)
    fun protocol(
        @PathParam("clientId") clientId: String
    ): ClientProtocol {
        clientService.ensureClient(clientId)
        return protocolService.globalProtocol(clientId)
    }

    @Path("protocol")
    @POST
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.CREATE)
    fun setProtocol(
        @Context uriInfo: UriInfo,
        clientProtocol: ClientProtocol,
    ): Response {
        clientService.ensureClient(clientProtocol.clientId)
        val updateResult = protocolService.setGlobalProtocol(clientProtocol)
        return updateResult.toResponse(uriInfo.baseUriBuilder)
    }

    @Path("protocol/{clientId}/contents")
    @PUT
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun setProtocol(
        @PathParam("clientId") clientId: String,
        @Context uriInfo: UriInfo,
        protocol: JsonNode,
    ): Response {
        clientService.ensureClient(clientId)
        val updateResult = protocolService.setGlobalProtocol(ClientProtocol(
            clientId = clientId,
            contents = protocol,
        ))
        return updateResult.toResponse(uriInfo.baseUriBuilder)
    }
}
