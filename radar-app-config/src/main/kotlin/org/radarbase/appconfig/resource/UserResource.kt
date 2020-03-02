package org.radarbase.appconfig.resource

import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.User
import org.radarbase.appconfig.domain.UserList
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ProjectAuthService
import org.radarbase.appconfig.service.UserService
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.auth.ProjectService
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarcns.auth.authorization.Permission
import javax.inject.Singleton
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType

/** Root path, just forward requests without authentication. */
@Path("/projects/{projectId}/users/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Singleton
class UserResource(
        @Context private val userService: UserService,
        @Context private val projectService: ProjectService,
        @Context private val clientService: ClientService
) {
    @GET
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ, "projectId")
    fun userClientConfig(
            @PathParam("projectId") projectId: String,
            @Context auth: Auth
    ): UserList {
        return UserList(userService.list(projectId)
                .filter { auth.token.hasPermissionOnSubject(Permission.SUBJECT_READ, projectId, it.id) })
    }

    @Path("/{userId}")
    @GET
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ, "projectId", "userId")
    fun userClientConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String
    ): User {
        return userService.find(projectId, userId)
                ?: throw HttpNotFoundException("user_missing", "User not found")
    }

    @Path("/{userId}/config/{clientId}")
    @GET
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ, "projectId", "userId")
    fun userClientConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String,
            @PathParam("clientId") clientId: String
    ): ClientConfig {
        clientService.ensureClient(clientId)
        userService.ensureUser(projectId, userId)
        return userService.userConfig(clientId, projectId, userId)
    }

    @Path("/{userId}/config/{clientId}")
    @POST
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ, "projectId", "userId")
    fun putUserClientConfig(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String,
            @PathParam("clientId") clientId: String,
            clientConfig: ClientConfig
    ): ClientConfig {
        clientService.ensureClient(clientId)
        userService.ensureUser(projectId, userId)
        userService.putUserConfig(clientId, userId, clientConfig)
        return userService.userConfig(clientId, projectId, userId)
    }
}
