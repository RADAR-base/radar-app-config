package org.radarbase.appconfig.filter

import org.radarbase.appconfig.auth.*
import org.radarbase.appconfig.service.ProjectService
import org.radarcns.auth.authorization.Permission
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo

/**
 * Check that the token has given permissions.
 */
class PermissionFilter : ContainerRequestFilter {

    @Context
    private lateinit var resourceInfo: ResourceInfo

    @Context
    private lateinit var auth: Auth

    @Context
    private lateinit var projectService: ProjectService

    @Context
    private lateinit var uriInfo: UriInfo

    override fun filter(requestContext: ContainerRequestContext) {
        val resourceMethod = resourceInfo.resourceMethod

        val userAnnotation = resourceMethod.getAnnotation(NeedsPermissionOnUser::class.java)
        val projectAnnotation = resourceMethod.getAnnotation(NeedsPermissionOnProject::class.java)
        val annotation = resourceMethod.getAnnotation(NeedsPermission::class.java)

        val (permission, project, isAuthenticated) = when {
            userAnnotation != null -> {
                val permission = Permission(userAnnotation.entity, userAnnotation.operation)
                val projectId = uriInfo.pathParameters[userAnnotation.projectPathParam]?.firstOrNull()
                val userId = uriInfo.pathParameters[userAnnotation.userPathParam]?.firstOrNull()

                Triple(permission, projectId, projectId != null
                        && userId != null
                        && auth.hasPermissionOnSubject(permission, projectId, userId))
            }
            projectAnnotation != null -> {
                val permission = Permission(projectAnnotation.entity, projectAnnotation.operation)

                val projectId = uriInfo.pathParameters[projectAnnotation.projectPathParam]?.firstOrNull()

                Triple(permission, projectId, projectId != null
                        && auth.hasPermissionOnProject(permission, projectId))
            }
            annotation != null -> {
                val permission = Permission(annotation.entity, annotation.operation)

                Triple(permission, null, auth.hasPermission(permission))
            }
            else -> return
        }

        if (!isAuthenticated) {
            abortWithForbidden(requestContext, permission)
            return
        }
        project?.let { projectService.ensureProject(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PermissionFilter::class.java)

        /**
         * Abort the request with a forbidden status. The caller must ensure that no other changes are
         * made to the context (i.e., make a quick return).
         * @param requestContext context to abort
         * @param scope the permission that is needed.
         */
        fun abortWithForbidden(requestContext: ContainerRequestContext, scope: Permission) {
            val message = "$scope permission not given."
            logger.warn("[403] {}: {}",
                    requestContext.uriInfo.path, message)

            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                            .header("WWW-Authenticate", AuthenticationFilter.BEARER_REALM
                                    + " error=\"insufficient_scope\""
                                    + " error_description=\"$message\""
                                    + " scope=\"$scope\"")
                            .build())
        }
    }
}
