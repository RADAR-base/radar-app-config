package org.radarbase.appconfig.inject

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.appconfig.service.*
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.config.JerseyResourceEnhancer
import jakarta.inject.Singleton

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

        bind(ConditionService::class.java)
            .to(ConditionService::class.java)
            .`in`(Singleton::class.java)

        bind(ConfigService::class.java)
            .to(ConfigService::class.java)
            .`in`(Singleton::class.java)

        bind(ConfigProjectServiceImpl::class.java)
            .to(ConfigProjectService::class.java)
            .`in`(Singleton::class.java)

        bind(ClientService::class.java)
            .to(ClientService::class.java)
            .`in`(Singleton::class.java)

        bind(ClientInterpreter::class.java)
            .to(ClientInterpreter::class.java)
            .`in`(Singleton::class.java)

        bind(UserService::class.java)
            .to(UserService::class.java)
            .`in`(Singleton::class.java)
    }
}
