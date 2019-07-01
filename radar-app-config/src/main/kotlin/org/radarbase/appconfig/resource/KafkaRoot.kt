package org.radarbase.appconfig.resource

import javax.inject.Singleton
import javax.ws.rs.OPTIONS
import javax.ws.rs.Path
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

/** Root path, just forward requests without authentication. */
@Path("/")
@Singleton
class KafkaRoot {
    @Context
//    private lateinit var proxyClient: ProxyClient

    val myMap: MutableMap<String, String?> = mutableMapOf()

    init {
        myMap["a"] = null
    }

    @OPTIONS
    fun rootOptions(): Response = Response.noContent()
            .header("Allow", "HEAD,GET,OPTIONS")
            .build()

//    @GET
//    fun root() = proxyClient.proxyRequest("GET")
//
//    @HEAD
//    fun rootHead() = proxyClient.proxyRequest("HEAD")
}


/**
 * Project admin user:
 * GET /clients
 * GET /projects
 * GET /projects/{project}
 * GET, POST (new condition), PUT (condition ordering) /projects/{project}/conditions
 * GET, PUT /projects/{project}/conditions/{condition}/config
 * GET, PUT /projects/{project}/conditions/{condition}/expression
 * GET, PUT /projects/{project}/config
 * GET /users/{userId}/config
 *
 * System admin user:
 * POST config  // set defaults
 *
 * Participant:
 * GET /config
 */

