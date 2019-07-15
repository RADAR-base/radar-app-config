package org.radarbase.appconfig.resource

import org.radarbase.appconfig.auth.Auth
import org.radarbase.appconfig.auth.Authenticated
import org.radarbase.appconfig.auth.NeedsPermission
import org.radarbase.appconfig.domain.OAuthClient
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.GlobalConfig
import org.radarbase.appconfig.domain.OAuthClientList
import org.radarbase.appconfig.exception.HttpApplicationException
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.appconfig.service.ProjectService
import org.radarcns.auth.authorization.Permission
import org.radarcns.auth.authorization.Permission.SUBJECT_READ
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

/** Root path, just forward requests without authentication. */
@Path("/")
@Produces("application/json; charset=utf-8")
@Consumes("application/json")
@Authenticated
class RootResource(
        @Context private val configService: ConfigService,
        @Context private val projectService: ProjectService,
        @Context private val clientService: ClientService
) {
    @Path("config")
    @GET
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ)
    fun config(@Context auth: Auth): GlobalConfig {
        val projectId = auth.defaultProject ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_missing", "Cannot request config without a project ID")
        val userId = auth.defaultProject ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_missing", "Cannot request config without a project ID")
        projectService.ensureProject(projectId)
        auth.hasPermissionOnSubject(SUBJECT_READ, projectId, userId)
        return configService.globalConfig(projectId, userId)
    }

    @Path("config/{clientId}")
    @GET
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ)
    fun clientConfig(
            @PathParam("clientId") clientId: String,
            @Context auth: Auth
    ): ClientConfig {
        val projectId = auth.defaultProject ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_missing", "Cannot request config without a project ID")
        val userId = auth.defaultProject ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_missing", "Cannot request config without a project ID")
        projectService.ensureProject(projectId)
        auth.hasPermissionOnSubject(SUBJECT_READ, projectId, userId)
        return configService.clientConfig(clientId, projectId, userId)
    }

    @Path("config")
    @PUT
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.CREATE)
    fun putConfig(config: GlobalConfig): Response {
        configService.putConfig(config)
        return Response.noContent().build()
    }

    @Path("clients")
    @GET
    @NeedsPermission(Permission.Entity.OAUTHCLIENTS, Permission.Operation.READ)
    fun clients(@Context mpClient: MPClient): OAuthClientList {
        return OAuthClientList(clientService.readClients().toList())
    }
}

/**
 * Project admin user:
 * GET /clients
 * GET /projects
 * GET /projects/{project}/users/{userId}/config
 * GET, POST (new condition), PUT (condition ordering) /projects/{project}/conditions
 * GET, PUT /projects/{project}/conditions/{condition}/config
 * GET, PUT /projects/{project}/conditions/{condition}/expression
 * GET, PUT /projects/{project}/config
 * GET /users/{userId}/config
 *
 * System admin user:
 * PUT config  // set defaults
 *
 * Participant:
 * GET /config
 */
