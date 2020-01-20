package org.radarbase.appconfig.inject

import org.radarbase.appconfig.Config
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.config.*

/** This binder needs to register all non-Jersey classes, otherwise initialization fails. */
class ManagementPortalEnhancerFactory(private val config: ApplicationConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> {
        val resolverEnhancer = InMemoryResourceEnhancer()
        return listOf(
                AppConfigResourceEnhancer(config),
                resolverEnhancer,
                ConfigLoader.Enhancers.radar(AuthConfig(
                        managementPortalUrl = config.authentication.url.toString(),
                        jwtResourceName = config.authentication.resourceName
                )),
                ConfigLoader.Enhancers.managementPortal,
                ConfigLoader.Enhancers.generalException,
                ConfigLoader.Enhancers.httpException)
    }
}
