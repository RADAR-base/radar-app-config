package org.radarbase.appconfig.inject

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.appconfig.service.*
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.config.JerseyResourceEnhancer
import jakarta.inject.Singleton
import org.radarbase.appconfig.domain.ProtocolMapper
import org.radarbase.jersey.service.ProjectService
import org.radarbase.jersey.service.managementportal.ProjectServiceWrapper
import org.radarbase.jersey.service.managementportal.RadarProjectService
import org.radarbase.management.client.MPOAuthClient

class AppConfigResourceEnhancer(private val config: ApplicationConfig) : JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = if (config.isCorsEnabled) arrayOf(
        ConfigLoader.Filters.cors,
        ConfigLoader.Filters.logResponse,
        ConfigLoader.Filters.cache,
    )
    else arrayOf(
        ConfigLoader.Filters.logResponse,
        ConfigLoader.Filters.cache,
    )

    override val packages: Array<String> = arrayOf("org.radarbase.appconfig.resource")

    override fun AbstractBinder.enhance() {
        // Bind instances. These cannot use any injects themselves
        bind(config)
            .to(ApplicationConfig::class.java)

        bind(ConfigService::class.java)
            .to(ConfigService::class.java)
            .`in`(Singleton::class.java)

        bind(ProtocolService::class.java)
            .to(ProtocolService::class.java)
            .`in`(Singleton::class.java)

        bind(ProtocolMapper::class.java)
            .to(ProtocolMapper::class.java)
            .`in`(Singleton::class.java)

        if (config.isAuthEnabled) {
            bind(MPClientService::class.java)
                .to(ClientService::class.java)
                .`in`(Singleton::class.java)
        } else {
            bind(FixedRadarProjectService::class.java)
                .to(RadarProjectService::class.java)
                .`in`(Singleton::class.java)

            bind(ProjectServiceWrapper::class.java)
                .to(ProjectService::class.java)
                .`in`(Singleton::class.java)

            val clients = mapOf(
                "aRMT" to MPOAuthClient(id = "aRMT"),
                "pRMT" to MPOAuthClient(id = "pRMT"),
            )
            bind(FixedClientService(clients))
                .to(ClientService::class.java)
        }
    }
}
