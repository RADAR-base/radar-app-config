package org.radarbase.appconfig.resource

import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigProjectService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.auth.authorization.Permission.Entity
import org.radarbase.auth.authorization.Permission.Operation
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.service.managementportal.RadarProjectService
import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.radarbase.appconfig.domain.*
import org.radarbase.jersey.cache.Cache
import org.radarbase.management.client.MPProject
import java.net.URI

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
    @Cache(maxAge = 300, isPrivate = true)
    @NeedsPermission(Entity.PROJECT, Operation.READ)
    fun listProjects(@Context auth: Auth) = ProjectList(
        radarProjectService.userProjects(auth)
            .map(MPProject::toProject)
    )

    @GET
    @NeedsPermission(Entity.PROJECT, Operation.READ, "projectId")
    @Path("{projectId}")
    @Cache(maxAge = 3600, isPrivate = true)
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

//    @Path("{projectId}/protocol/{clientId}")
//    @GET
//    @NeedsPermission(Entity.PROJECT, Operation.READ, "projectId")
//    fun protocol(
//        @PathParam("projectId") projectId: String,
//        @PathParam("clientId") clientId: String,
//    ): ClientProtocol {
//        clientService.ensureClient(clientId)
//        return protocolService.globalProtocol(clientId)
//    }
//
//    @Path("protocol")
//    @POST
//    @NeedsPermission(Entity.PROJECT, Operation.UPDATE, "projectId")
//    fun setProtocol(
//        @PathParam("projectId") projectId: String,
//        @Context uriInfo: UriInfo,
//        clientProtocol: ClientProtocol,
//    ): Response {
//        clientService.ensureClient(clientProtocol.clientId)
//        val didExist = protocolService.setGlobalProtocol(clientProtocol)
//        return if (didExist) {
//            Response.notModified()
//        } else {
//            Response.created(URI.create("${uriInfo.path}/${clientProtocol.clientId}"))
//        }.build()
//    }
}
