package org.radarbase.appconfig.resource

import org.radarbase.appconfig.domain.OAuthClientList
import org.radarbase.appconfig.service.ClientService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarcns.auth.authorization.Permission
import javax.inject.Singleton
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType

/** Root path, just forward requests without authentication. */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Singleton
class RootResource(
        @Context private val clientService: ClientService
) {
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
