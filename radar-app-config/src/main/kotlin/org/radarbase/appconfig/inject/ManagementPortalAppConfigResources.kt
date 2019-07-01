package org.radarbase.appconfig.inject

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.appconfig.auth.AuthValidator
import javax.inject.Singleton

/** This binder needs to register all non-Jersey classes, otherwise initialization fails. */
class ManagementPortalAppConfigResources : AppConfigResources {
    override fun registerAuthentication(resources: ResourceConfig) {
        // none needed
    }

    override fun registerAuthenticationUtilities(binder: AbstractBinder) {
        binder.bind(RadarTokenValidator::class.java)
                .to(AuthValidator::class.java)
                .`in`(Singleton::class.java)
    }
}
