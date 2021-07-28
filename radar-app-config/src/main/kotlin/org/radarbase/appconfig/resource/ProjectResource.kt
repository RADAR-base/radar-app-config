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
import org.radarbase.auth.authorization.Permission.Entity
import org.radarbase.auth.authorization.Permission.Operation
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.cache.Cache
import org.radarbase.jersey.service.managementportal.RadarProjectService
import org.radarbase.management.client.MPProject

@Path("projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
@Authenticated
class ProjectResource(
    @Context private val projectService: RadarProjectService,
    @Context private val configService: ConfigService,
    @Context private val clientService: ClientService,
    @Context private val protocolService: ProtocolService,
) {
    @GET
    @Cache(maxAge = 300, isPrivate = true)
    @NeedsPermission(Entity.PROJECT, Operation.READ)
    fun listProjects(@Context auth: Auth) = ProjectList(
        projectService.userProjects(auth)
            .map(MPProject::toProject)
    )

    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ, "projectId")
    @Path("{projectId}")
    @Cache(maxAge = 3600, isPrivate = true)
    fun get(@PathParam("projectId") projectId: String): Project =
        projectService.project(projectId).toProject()

    @Path("{projectId}/config/{clientId}")
    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ, "projectId")
    fun projectConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("clientId") clientId: String,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        return configService.projectConfig(clientId, projectId)
    }

    @Path("{projectId}/config/{clientId}")
    @POST
    @NeedsPermission(Entity.PROJECT, Operation.UPDATE, "projectId")
    fun putProjectConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("clientId") clientId: String,
        clientConfig: ClientConfig,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        configService.putProjectConfig(clientId, projectId, clientConfig)
        return configService.projectConfig(clientId, projectId)
    }

    @Path("{projectId}/protocols/{clientId}")
    @GET
    @Cache(maxAge = 300, isPrivate = true)
    @NeedsPermission(Entity.PROJECT, Operation.READ, "projectId")
    fun protocol(
        @PathParam("projectId") projectId: String,
        @PathParam("clientId") clientId: String,
    ): ClientProtocol {
        clientService.ensureClient(clientId)
        projectService.ensureProject(projectId)
        return protocolService.projectProtocol(clientId, projectId)
    }

    @Path("{projectId}/protocols/{clientId}/contents")
    @GET
    @Cache(maxAge = 300, isPrivate = true)
    @NeedsPermission(Entity.PROJECT, Operation.READ, "projectId")
    fun protocolContents(
        @PathParam("projectId") projectId: String,
        @PathParam("clientId") clientId: String,
    ): JsonNode {
        clientService.ensureClient(clientId)
        projectService.ensureProject(projectId)
        return protocolService.projectProtocol(clientId, projectId).contents
    }

    @Path("{projectId}/protocols")
    @POST
    @NeedsPermission(Entity.PROJECT, Operation.UPDATE, "projectId")
    fun setProtocol(
        @PathParam("projectId") projectId: String,
        @Context uriInfo: UriInfo,
        clientProtocol: ClientProtocol,
    ): Response {
        clientService.ensureClient(clientProtocol.clientId)
        projectService.ensureProject(projectId)
        val updateResult = protocolService.setProjectProtocol(clientProtocol, projectId)
        return updateResult.toResponse(uriInfo.baseUriBuilder)
    }

    @Path("{projectId}/protocols/{clientId}/contents")
    @PUT
    @NeedsPermission(Entity.PROJECT, Operation.UPDATE, "projectId")
    fun setProtocol(
        @PathParam("projectId") projectId: String,
        @PathParam("clientId") clientId: String,
        protocol: JsonNode,
    ): Response {
        clientService.ensureClient(clientId)
        projectService.ensureProject(projectId)
        protocolService.setProjectProtocol(
            ClientProtocol(
                clientId = clientId,
                contents = protocol,
            ),
            projectId,
        )
        return Response.notModified().build()
    }
}
