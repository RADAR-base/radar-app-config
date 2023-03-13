package org.radarbase.appconfig.resource

import io.ktor.http.*
import jakarta.inject.Singleton
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION
import jakarta.ws.rs.core.MediaType
import org.radarbase.appconfig.api.OAuthClientList
import org.radarbase.appconfig.api.toOAuthClient
import org.radarbase.appconfig.service.ClientService
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.cache.Cache
import org.radarbase.jersey.coroutines.runAsCoroutine
import org.radarbase.management.client.MPOAuthClient

/** Root path, just forward requests without authentication. */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Singleton
class RootResource(
    @Context private val clientService: ClientService,
) {
    @Path("clients")
    @GET
    @Cache(maxAge = 3600, isPrivate = true, vary = [AUTHORIZATION])
    @NeedsPermission(Permission.OAUTHCLIENTS_READ)
    fun clients(
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncResponse.runAsCoroutine {
        OAuthClientList(
            clientService.readClients()
                .map(MPOAuthClient::toOAuthClient)
        )
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
