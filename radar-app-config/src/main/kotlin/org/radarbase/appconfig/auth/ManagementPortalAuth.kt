package org.radarbase.appconfig.auth

import org.radarbase.appconfig.exception.HttpApplicationException
import org.radarcns.auth.authorization.Permission
import org.radarcns.auth.authorization.Permission.MEASUREMENT_CREATE
import org.radarcns.auth.token.RadarToken
import javax.ws.rs.core.Response

/**
 * Parsed JWT for validating authorization of data contents.
 */
class ManagementPortalAuth(private val token: RadarToken) : Auth {
    override val defaultProject = token.roles.keys
            .firstOrNull { token.hasPermissionOnProject(MEASUREMENT_CREATE, it) }
    override val userId: String? = token.subject.takeUnless { it.isEmpty() }

    override fun checkPermission(projectId: String?, userId: String?, sourceId: String?) {
        if (!token.hasPermissionOnSource(MEASUREMENT_CREATE,
                        projectId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "project_id_missing", "Missing project ID in request"),
                        userId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "user_id_missing", "Missing user ID in request"),
                        sourceId ?: throw HttpApplicationException(Response.Status.BAD_REQUEST, "source_id_missing", "Missing source ID in request"))) {
            throw HttpApplicationException(Response.Status.FORBIDDEN, "permission_mismatch", "No permission to create measurement for " +
                    "project $projectId with user $userId and source $sourceId " +
                    "using token ${token.token}")
        }
    }

    override fun hasRole(projectId: String, role: String) = token.roles
            .getOrDefault(projectId, emptyList())
            .contains(role)

    override fun hasPermission(permission: Permission) = token.hasPermission(permission)
}
