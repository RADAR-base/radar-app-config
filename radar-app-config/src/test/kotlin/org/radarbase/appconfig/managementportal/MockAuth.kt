package org.radarbase.appconfig.managementportal

import org.radarbase.appconfig.auth.Auth
import org.radarcns.auth.authorization.Permission

class MockAuth : Auth {
    override var clientId: String = "appconfig"
    var hasPermission = true

    override var defaultProject: String? = null

    override var userId: String? = null

    override fun hasPermissionOnProject(permission: Permission, projectId: String) = hasPermission

    override fun hasPermissionOnSubject(permission: Permission, projectId: String, userId: String) = hasPermission

    override fun checkPermission(projectId: String?, userId: String?, sourceId: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasRole(projectId: String, role: String) = hasPermission

    override fun hasPermission(permission: Permission) = hasPermission
}