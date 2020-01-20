package org.radarbase.appconfig.resource

import org.radarbase.appconfig.domain.OAuthClientList
import org.radarbase.appconfig.domain.ProjectList
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConfigProjectService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.auth.ProjectService
import org.radarcns.auth.authorization.Permission
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context

/** Root path, just forward requests without authentication. */
@Path("/")
@Produces("application/json; charset=utf-8")
@Consumes("application/json")
@Authenticated
class RootResource(
        @Context private val configService: ConfigService,
        @Context private val projectAuthService: ProjectService,
        @Context private val projectService: ConfigProjectService,
        @Context private val clientService: ClientService
) {
    @Path("projects")
    @GET
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ)
    fun listProjects() = ProjectList(projectService.listProjects().toList())

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
