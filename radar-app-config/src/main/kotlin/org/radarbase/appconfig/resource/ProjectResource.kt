package org.radarbase.appconfig.resource

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.radarbase.appconfig.api.*
import org.radarbase.appconfig.api.toProject
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.appconfig.service.ProtocolService
import org.radarbase.appconfig.service.ProtocolService.Companion.toResponse
import org.radarbase.auth.authorization.Permission.Entity.*
import org.radarbase.auth.authorization.Permission.Operation.*
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
    @NeedsPermission(PROJECT, READ)
    @Operation(
        description = "List projects",
        responses = [ApiResponse(description = "List of projects", responseCode = "200")],
    )
    fun listProjects(@Context auth: Auth) = ProjectList(
        projectService.userProjects(auth)
            .map(MPProject::toProject)
    )

    @GET
    @NeedsPermission(PROJECT, READ, "projectId")
    @Path("{projectId}")
    @Cache(maxAge = 3600, isPrivate = true)
    @Operation(
        description = "Get a projects",
        responses = [ApiResponse(description = "Project description", responseCode = "200"), ],
    )
    fun get(@PathParam("projectId") projectId: String): Project =
        projectService.project(projectId).toProject()

    @Path("{projectId}/config/{clientId}")
    @GET
    @NeedsPermission(PROJECT, READ, "projectId")
    @Operation(
        description = "Get the configuration for a given project and client",
        responses = [ApiResponse(description = "Project configuration", responseCode = "200")],
    )
    fun projectConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("clientId") clientId: String,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        return configService.projectConfig(clientId, projectId)
    }

    @Path("{projectId}/config/{clientId}")
    @POST
    @NeedsPermission(PROJECT, UPDATE, "projectId")
    @Operation(
        description = "Update the configuration for a given project and client",
        responses = [ApiResponse(description = "Configuration", responseCode = "200")],
        requestBody = RequestBody(description = "Valid configuration object, containing at least a config field with a list of variables.")
    )
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
    @NeedsPermission(PROJECT, READ, "projectId")
    @Operation(
        description = "Get the protocol for a given project and client",
        responses = [ApiResponse(description = "Protocol", responseCode = "200")],
    )
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
    @NeedsPermission(PROJECT, READ, "projectId")
    @Operation(
        description = "Get the protocol contents for a given project and client",
        responses = [ApiResponse(description = "Protocol contents", responseCode = "200")],
    )
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
    @NeedsPermission(PROJECT, UPDATE, "projectId")
    @Operation(
        description = "Set the protocol for a given project and client",
        responses = [
            ApiResponse(description = "Location of the newly created protocol", responseCode = "201", headers = [
                Header(name = "Location", description = "Location of the created protocol"),
            ]),
            ApiResponse(description = "Location of the existing identical protocol", responseCode = "304", headers = [
                Header(name = "Location", description = "Location of the existing protocol"),
            ]),
            ApiResponse(description = "The protocol did not match the schema.", responseCode = "400"),
        ],
        requestBody = RequestBody(description = "A client protocol, including client ID and contents.")
    )
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
    @NeedsPermission(PROJECT, UPDATE, "projectId")
    @Operation(
        description = "Update only the protocol contents for a given project and client",
        responses = [
            ApiResponse(description = "The contents were updated", responseCode = "204"),
            ApiResponse(description = "The protocol did not match the schema.", responseCode = "400"),
        ],
        requestBody = RequestBody(description = "Protocol contents matching the JSON schema.")
    )
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
