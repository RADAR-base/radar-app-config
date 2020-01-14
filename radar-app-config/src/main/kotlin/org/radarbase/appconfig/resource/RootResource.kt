package org.radarbase.appconfig.resource

import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.GlobalConfig
import org.radarbase.appconfig.domain.OAuthClientList
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.auth.ProjectService
import org.radarbase.jersey.exception.HttpBadRequestException
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
        @Context private val projectAuthService: ProjectService,
        @Context private val clientService: ClientService
) {
    @Path("config")
    @GET
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ)
    fun config(@Context auth: Auth): GlobalConfig {
        val (projectId, userId) = ensureUser(auth)
        return configService.globalConfig(projectId, userId)
    }

    @Path("config/clients/{clientId}")
    @GET
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ)
    fun clientConfig(
            @PathParam("clientId") clientId: String,
            @Context auth: Auth
    ): ClientConfig {
        val (projectId, userId) = ensureUser(auth)
        return configService.clientConfig(clientId, projectId, userId)
    }

    private fun ensureUser(auth: Auth): Pair<String, String> {
        val projectId = auth.defaultProject ?: throw HttpBadRequestException("project_missing", "Cannot request config without a project ID")
        val userId = auth.userId ?: throw HttpBadRequestException("user_missing", "Cannot request config without a user ID")
        projectAuthService.ensureProject(projectId)
        auth.checkPermissionOnSubject(SUBJECT_READ, projectId, userId)
        return projectId to userId
    }

    @Path("clients")
    @GET
    @NeedsPermission(Permission.Entity.OAUTHCLIENTS, Permission.Operation.READ)
    fun clients(): OAuthClientList {
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
