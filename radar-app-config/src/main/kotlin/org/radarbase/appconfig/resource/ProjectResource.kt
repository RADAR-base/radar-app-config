package org.radarbase.appconfig.resource

import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.GlobalConfig
import org.radarbase.appconfig.domain.ProjectList
import org.radarbase.appconfig.managementportal.MPClient
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
        @Context private val client: MPClient,
        @Context private val projectService: ConfigProjectService,
        @Context private val configService: ConfigService
) {
    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ)
    fun listProjects() = ProjectList(client.readProjects())

    @Path("{projectId}/config")
    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ, "projectId")
    fun projectConfig(@PathParam("projectId") projectId: String): GlobalConfig {
        return projectService.projectConfig(projectId)
    }

    @Path("{projectId}/config")
    @PUT
    @NeedsPermission(Entity.PROJECT, Operation.UPDATE, "projectId")
    fun putProjectConfig(
            @PathParam("projectId") projectId: String,
            globalConfig: GlobalConfig
    ): Response {
        projectService.putConfig(projectId, globalConfig)

        return Response.noContent().build()
    }

    @Path("{projectId}/users/{userId}/config")
    @GET
    @NeedsPermission(Entity.SUBJECT, Operation.READ, "projectId", "userId")
    fun userConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String
    ): GlobalConfig {
        return configService.globalConfig(projectId, userId)
    }

    @Path("{projectId}/users/{userId}/config/{clientId}")
    @GET
    @NeedsPermission(Entity.SUBJECT, Operation.READ, "projectId", "userId")
    fun userClientConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String,
            @PathParam("clientId") clientId: String
    ): ClientConfig {
        return configService.clientConfig(clientId, projectId, userId)
    }

}