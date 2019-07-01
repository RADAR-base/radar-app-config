package org.radarbase.appconfig.filter

import org.radarcns.auth.authorization.Permission
import org.radarbase.appconfig.auth.Auth
import org.radarbase.appconfig.auth.AuthenticationFilter
import org.radarbase.appconfig.auth.NeedsPermission
import org.slf4j.LoggerFactory
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

/**
 * Check that the token has given permissions.
 */
class PermissionFilter : ContainerRequestFilter {

    @Context
    private lateinit var resourceInfo: ResourceInfo

    @Context
    private lateinit var auth: Auth

    override fun filter(requestContext: ContainerRequestContext) {
        val annotation = resourceInfo.resourceMethod.getAnnotation(NeedsPermission::class.java)
        val permission = Permission(annotation.entity, annotation.operation)

        if (!auth.hasPermission(permission)) {
            abortWithForbidden(requestContext, permission)
        }
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
