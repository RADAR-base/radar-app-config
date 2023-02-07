package org.radarbase.appconfig.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION
import jakarta.ws.rs.core.MediaType
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.User
import org.radarbase.appconfig.api.UserList
import org.radarbase.appconfig.api.toUser
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.UserService
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.cache.Cache
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.managementportal.RadarProjectService
import org.radarbase.management.client.MPSubject

/** Root path, just forward requests without authentication. */
@Path("/projects/{projectId}/users/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Singleton
class UserResource(
    @Context private val userService: UserService,
    @Context private val clientService: ClientService,
    @Context private val radarProjectService: RadarProjectService,
) {
    @GET
    @Cache(maxAge = 60, isPrivate = true, vary = [AUTHORIZATION])
    @NeedsPermission(Permission.SUBJECT_READ, "projectId")
    fun userClientConfig(
        @PathParam("projectId") projectId: String,
    ): UserList {
        return UserList(
            radarProjectService.projectSubjects(projectId)
                .map(MPSubject::toUser)
        )
    }

    @Path("/{userId}")
    @GET
    @Cache(maxAge = 60, isPrivate = true, vary = [AUTHORIZATION])
    @NeedsPermission(Permission.SUBJECT_READ, "projectId", "userId")
    fun userClientConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
    ): User {
        return radarProjectService.subject(projectId, userId)?.toUser()
            ?: throw HttpNotFoundException("user_missing", "User not found")
    }

    @Path("/{userId}/config/{clientId}")
    @GET
    @NeedsPermission(Permission.SUBJECT_READ, "projectId", "userId")
    fun userClientConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
        @PathParam("clientId") clientId: String,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        return userService.userConfig(clientId, projectId, userId)
    }

    @Path("/{userId}/config/{clientId}")
    @POST
    @NeedsPermission(Permission.SUBJECT_READ, "projectId", "userId")
    fun putUserClientConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("userId") userId: String,
        @PathParam("clientId") clientId: String,
        clientConfig: ClientConfig,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        userService.putUserConfig(clientId, userId, clientConfig)
        return userService.userConfig(clientId, projectId, userId)
    }
}
