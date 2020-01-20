package org.radarbase.appconfig.resource

import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.ProjectList
import org.radarbase.appconfig.service.ConfigProjectService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarcns.auth.authorization.Permission.Entity
import org.radarcns.auth.authorization.Permission.Operation
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

@Path("projects")
@Produces("application/json; charset=utf-8")
@Consumes("application/json")
@Authenticated
class ProjectResource(
        @Context private val projectService: ConfigProjectService,
        @Context private val configService: ConfigService
) {
    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ)
    fun listProjects() = ProjectList(projectService.listProjects().toList())

    @Path("{projectId}/config/{clientId}")
    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ, "projectId")
    fun projectConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("clientId") clientId: String
    ): ClientConfig {
        return projectService.projectConfig(clientId, projectId)
    }

    @Path("{projectId}/config/{clientId}")
    @PUT
    @NeedsPermission(Entity.PROJECT, Operation.UPDATE, "projectId")
    fun putProjectConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("clientId") clientId: String,
            clientConfig: ClientConfig
    ): Response {
        projectService.putProjectConfig(clientId, projectId, clientConfig)

        return Response.noContent().build()
    }

    @Path("{projectId}/users/{userId}/config/{clientId}")
    @GET
    @NeedsPermission(Entity.SUBJECT, Operation.READ, "projectId", "userId")
    fun userClientConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String,
            @PathParam("clientId") clientId: String
    ): ClientConfig {
        return projectService.userConfig(clientId, projectId, userId)
    }

    @Path("{projectId}/users/{userId}/config/{clientId}")
    @PUT
    @NeedsPermission(Entity.SUBJECT, Operation.READ, "projectId", "userId")
    fun putUserClientConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String,
            @PathParam("clientId") clientId: String,
            clientConfig: ClientConfig
    ): Response {
        projectService.putUserConfig(clientId, userId, clientConfig)

        return Response.noContent().build()
    }
}