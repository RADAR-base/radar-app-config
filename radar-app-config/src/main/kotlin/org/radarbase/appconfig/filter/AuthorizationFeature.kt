package org.radarbase.appconfig.filter

import org.radarbase.appconfig.auth.NeedsPermission
import org.radarbase.appconfig.auth.NeedsPermissionOnProject
import org.radarbase.appconfig.auth.NeedsPermissionOnUser
import javax.ws.rs.Priorities
import javax.ws.rs.container.DynamicFeature
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.FeatureContext
import javax.ws.rs.ext.Provider

/** Authorization for different auth tags. */
@Provider
class AuthorizationFeature : DynamicFeature {
    override fun configure(resourceInfo: ResourceInfo, context: FeatureContext) {
        val resourceMethod = resourceInfo.resourceMethod
        if (resourceMethod.isAnnotationPresent(NeedsPermission::class.java)
                || resourceMethod.isAnnotationPresent(NeedsPermissionOnProject::class.java)
                || resourceMethod.isAnnotationPresent(NeedsPermissionOnUser::class.java)) {
            context.register(PermissionFilter::class.java, Priorities.AUTHORIZATION)
        }
    }
}
