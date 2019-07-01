package org.radarbase.appconfig.auth

import org.radarcns.auth.authorization.Permission

interface Auth {
    val defaultProject: String?
    val userId: String?

    fun checkPermission(projectId: String?, userId: String?, sourceId: String?)
    fun hasRole(projectId: String, role: String): Boolean
    fun hasPermission(permission: Permission): Boolean
}
