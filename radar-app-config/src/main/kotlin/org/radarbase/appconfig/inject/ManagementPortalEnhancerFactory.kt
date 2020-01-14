package org.radarbase.appconfig.inject

import org.radarbase.appconfig.Config
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.config.*

/** This binder needs to register all non-Jersey classes, otherwise initialization fails. */
class ManagementPortalEnhancerFactory(private val config: Config) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> = listOf(
            AppConfigResourceEnhancer(config),
            ConfigLoader.Enhancers.radar(config = AuthConfig(
                    managementPortalUrl = config.managementPortalUrl.toString(),
                    jwtResourceName = config.jwtResourceName
            )),
            ConfigLoader.Enhancers.managementPortal,
            ConfigLoader.Enhancers.generalException,
            ConfigLoader.Enhancers.httpException)
}
