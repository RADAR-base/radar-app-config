package org.radarbase.appconfig.auth

import org.radarcns.auth.authorization.Permission

interface Auth {
    val clientId: String?
    val defaultProject: String?
    val userId: String?

    fun hasPermissionOnProject(permission: Permission, projectId: String): Boolean
    fun hasPermissionOnSubject(permission: Permission, projectId: String, userId: String): Boolean
    fun checkPermission(projectId: String?, userId: String?, sourceId: String?)
    fun hasRole(projectId: String, role: String): Boolean
    fun hasPermission(permission: Permission): Boolean
}
