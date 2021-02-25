package org.radarbase.appconfig.resource

import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.Project
import org.radarbase.appconfig.domain.ProjectList
import org.radarbase.appconfig.domain.toProject
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigProjectService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.service.managementportal.RadarProjectService
import org.radarcns.auth.authorization.Permission.Entity
import org.radarcns.auth.authorization.Permission.Operation
import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import org.radarbase.management.client.MPProject

@Path("projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
@Authenticated
class ProjectResource(
    @Context private val radarProjectService: RadarProjectService,
    @Context private val projectService: ConfigProjectService,
    @Context private val configService: ConfigService,
    @Context private val clientService: ClientService,
) {
    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ)
    fun listProjects(@Context auth: Auth) = ProjectList(
        radarProjectService.userProjects(auth)
            .map(MPProject::toProject)
    )

    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ, "projectId")
    @Path("{projectId}")
    fun get(@PathParam("projectId") projectId: String): Project =
        radarProjectService.project(projectId).toProject()

    @Path("{projectId}/config/{clientId}")
    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ, "projectId")
    fun projectConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("clientId") clientId: String,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        return projectService.projectConfig(clientId, projectId)
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
        projectService.putProjectConfig(clientId, projectId, clientConfig)
        return projectService.projectConfig(clientId, projectId)
    }
}
