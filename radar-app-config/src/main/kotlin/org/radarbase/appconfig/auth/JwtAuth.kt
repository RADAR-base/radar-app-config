package org.radarbase.appconfig.auth

import com.auth0.jwt.interfaces.DecodedJWT
import org.radarbase.appconfig.exception.HttpApplicationException
import org.radarcns.auth.authorization.Permission
import org.radarcns.auth.authorization.Permission.*
import javax.ws.rs.core.Response

/**
 * Parsed JWT for validating authorization of data contents.
 */
class JwtAuth(project: String?, private val token: DecodedJWT, private val scopes: List<String>) : Auth {
    private val claimProject = token.getClaim("project").asString()
    override val clientId: String? = token.getClaim("client_id").asString() ?: "appconfig_frontend"
    override val defaultProject = claimProject ?: project
    override val userId: String? = token.subject?.takeUnless { it.isEmpty() }

    override fun checkPermission(projectId: String?, userId: String?, sourceId: String?) {
        if (!hasPermission(MEASUREMENT_CREATE)) {
            throw HttpApplicationException(Response.Status.FORBIDDEN, "permission_mismatch", "No permission to create measurement " +
                    "using token ${token.token}")
        }

        if (userId != null && this.userId != null && userId != this.userId) {
            throw HttpApplicationException(Response.Status.FORBIDDEN, "permission_mismatch", "Actual user ID $userId does not match expected user ID ${this.userId} " +
                    "using token ${token.token}")
        }

        if (projectId != null && claimProject != null && projectId != claimProject) {
            throw HttpApplicationException(Response.Status.FORBIDDEN, "permission_mismatch", "Actual project ID $projectId does not match expected project ID ${this.claimProject} " +
                    "using token ${token.token}")
        }
    }

    override fun hasPermissionOnProject(permission: Permission, projectId: String): Boolean {
        return hasPermission(permission) && (claimProject != null && projectId == claimProject)
    }

    override fun hasPermissionOnSubject(permission: Permission, projectId: String, userId: String): Boolean {
        return hasPermissionOnProject(permission, projectId) && (userId == this.userId || hasPermission(PROJECT_UPDATE))
    }

    override fun hasRole(projectId: String, role: String) = projectId == defaultProject

    override fun hasPermission(permission: Permission) = scopes.contains(permission.scopeName())
}
