package org.radarbase.appconfig.resource

import org.radarbase.appconfig.auth.Authenticated
import org.radarbase.appconfig.auth.NeedsPermission
import org.radarbase.appconfig.auth.NeedsPermissionOnProject
import org.radarbase.appconfig.auth.NeedsPermissionOnUser
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.GlobalConfig
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.appconfig.service.ProjectService
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
        @Context private val projectService: ProjectService,
        @Context private val configService: ConfigService
) {
    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ)
    fun listProjects() = client.readProjects()

    @Path("{projectId}/config")
    @GET
    @NeedsPermissionOnProject(Entity.PROJECT, Operation.READ, "projectId")
    fun projectConfig(@PathParam("projectId") projectId: String): GlobalConfig {
        return projectService.projectConfig(projectId)
    }

    @Path("{projectId}/config")
    @PUT
    @NeedsPermissionOnProject(Entity.PROJECT, Operation.UPDATE, "projectId")
    fun putProjectConfig(
            @PathParam("projectId") projectId: String,
            globalConfig: GlobalConfig
    ): Response {
        projectService.putConfig(projectId, globalConfig)

        return Response.noContent().build()
    }

    @Path("{projectId}/users/{userId}/config")
    @GET
    @NeedsPermissionOnUser(Entity.SUBJECT, Operation.READ, "projectId", "userId")
    fun userConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String
    ): GlobalConfig {
        return configService.globalConfig(projectId, userId)
    }

    @Path("{projectId}/users/{userId}/config/{clientId}")
    @GET
    @NeedsPermissionOnUser(Entity.SUBJECT, Operation.READ, "projectId", "userId")
    fun userClientConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String,
            @PathParam("clientId") clientId: String
    ): ClientConfig {
        return configService.clientConfig(clientId, projectId, userId)
    }

}