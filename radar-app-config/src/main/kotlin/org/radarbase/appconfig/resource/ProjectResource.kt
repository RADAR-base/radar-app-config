package org.radarbase.appconfig.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION
import jakarta.ws.rs.core.MediaType
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.ProjectList
import org.radarbase.appconfig.api.toProject
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigProjectService
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.cache.Cache
import org.radarbase.jersey.coroutines.runAsCoroutine
import org.radarbase.jersey.service.managementportal.RadarProjectService
import org.radarbase.management.client.MPProject

@Path("projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
@Authenticated
class ProjectResource(
    @Context private val radarProjectService: RadarProjectService,
    @Context private val projectService: ConfigProjectService,
    @Context private val clientService: ClientService,
) {
    @GET
    @Cache(maxAge = 300, isPrivate = true, vary = [AUTHORIZATION])
    @NeedsPermission(Permission.PROJECT_READ)
    fun listProjects(@Suspended asyncResponse: AsyncResponse) = asyncResponse.runAsCoroutine {
        ProjectList(
            radarProjectService.userProjects()
                .map(MPProject::toProject)
        )
    }

    @GET
    @NeedsPermission(Permission.PROJECT_READ, "projectId")
    @Path("{projectId}")
    @Cache(maxAge = 3600, isPrivate = true, vary = [AUTHORIZATION])
    fun get(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("projectId") projectId: String,
    ) = asyncResponse.runAsCoroutine {
        radarProjectService.project(projectId).toProject()
    }

    @Path("{projectId}/config/{clientId}")
    @GET
    @NeedsPermission(Permission.PROJECT_READ, "projectId")
    fun projectConfig(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("projectId") projectId: String,
        @PathParam("clientId") clientId: String,
    ) = asyncResponse.runAsCoroutine {
        clientService.ensureClient(clientId)
        projectService.projectConfig(clientId, projectId)
    }

    @Path("{projectId}/config/{clientId}")
    @POST
    @NeedsPermission(Permission.PROJECT_UPDATE, "projectId")
    fun putProjectConfig(
        @Suspended asyncResponse: AsyncResponse,
        @PathParam("projectId") projectId: String,
        @PathParam("clientId") clientId: String,
        clientConfig: ClientConfig,
    ) = asyncResponse.runAsCoroutine {
        clientService.ensureClient(clientId)
        projectService.putProjectConfig(clientId, projectId, clientConfig)
        projectService.projectConfig(clientId, projectId)
    }
}
