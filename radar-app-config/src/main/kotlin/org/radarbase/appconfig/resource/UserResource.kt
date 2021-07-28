package org.radarbase.appconfig.resource

import com.fasterxml.jackson.databind.JsonNode
import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.radarbase.appconfig.domain.*
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.appconfig.service.ProtocolService
import org.radarbase.appconfig.service.ProtocolService.Companion.toResponse
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.cache.Cache
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.managementportal.RadarProjectService
import org.radarbase.management.client.MPSubject

/** Root path, just forward requests without authentication. */
@Path("/projects/{projectId}/users/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Singleton
class UserResource(
    @Context private val configService: ConfigService,
    @Context private val clientService: ClientService,
    @Context private val projectService: RadarProjectService,
    @Context private val protocolService: ProtocolService,
) {
    @GET
    @Cache(maxAge = 60, isPrivate = true)
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ, "projectId")
    fun userClientConfig(
        @PathParam("projectId") projectId: String,
    ): UserList {
        return UserList(
            projectService.projectUsers(projectId)
                .map(MPSubject::toUser)
        )
    }

    @Path("/{userId}")
    @GET
    @Cache(maxAge = 60, isPrivate = true)
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ, "projectId", "userId")
    fun userClientConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
    ): User {
        return projectService.getUser(projectId, userId)?.toUser()
            ?: throw HttpNotFoundException("user_not_found", "User $userId not found in project $projectId")
    }

    @Path("/{userId}/config/{clientId}")
    @GET
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ, "projectId", "userId")
    fun userClientConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
        @PathParam("clientId") clientId: String,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        projectService.ensureUser(projectId, userId)
        return configService.userConfig(clientId, projectId, userId)
    }

    @Path("/{userId}/config/{clientId}")
    @POST
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ, "projectId", "userId")
    fun putUserClientConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
        @PathParam("clientId") clientId: String,
        clientConfig: ClientConfig,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        projectService.ensureUser(projectId, userId)
        configService.putUserConfig(clientId, userId, clientConfig)
        return configService.userConfig(clientId, projectId, userId)
    }

    @Path("{userId}/protocol/{clientId}")
    @Cache(maxAge = 300, isPrivate = true)
    @GET
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    fun protocol(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
        @PathParam("clientId") clientId: String,
    ): ClientProtocol {
        clientService.ensureClient(clientId)
        projectService.ensureUser(projectId, userId)
        return protocolService.userProtocol(clientId, projectId, userId)
    }

    @Path("/{userId}/protocol")
    @POST
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun setProtocol(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
        @Context uriInfo: UriInfo,
        clientProtocol: ClientProtocol,
    ): Response {
        clientService.ensureClient(clientProtocol.clientId)
        projectService.ensureUser(projectId, userId)
        val updateResult = protocolService.setUserProtocol(clientProtocol, userId)
        return updateResult.toResponse(uriInfo.baseUriBuilder)
    }

    @Path("/{userId}/protocol/{clientId}/contents")
    @PUT
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun setProtocol(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
        @PathParam("clientId") clientId: String,
        @Context uriInfo: UriInfo,
        protocol: JsonNode,
    ): Response {
        clientService.ensureClient(clientId)
        projectService.ensureUser(projectId, userId)
        val updateResult = protocolService.setUserProtocol(
            ClientProtocol(
                clientId = clientId,
                contents = protocol,
            ),
            userId,
        )
        return updateResult.toResponse(uriInfo.baseUriBuilder)
    }
}
