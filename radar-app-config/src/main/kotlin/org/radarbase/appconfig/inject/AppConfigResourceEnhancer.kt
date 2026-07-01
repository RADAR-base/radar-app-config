package org.radarbase.appconfig.inject

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConditionService
import org.radarbase.appconfig.service.GlobalConfigService
import org.radarbase.appconfig.service.NonResolvingConfigService
import org.radarbase.appconfig.service.NonResolvingConfigServiceImpl
import org.radarbase.appconfig.service.ProjectConfigService
import org.radarbase.appconfig.service.ProjectConfigServiceImpl
import org.radarbase.appconfig.service.UserConfigService
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.filter.Filters

class AppConfigResourceEnhancer(private val config: ApplicationConfig) : JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = if (config.isCorsEnabled) {
        arrayOf(
            Filters.cors,
            Filters.logResponse,
            Filters.cache,
        )
    } else {
        arrayOf(
            Filters.logResponse,
            Filters.cache,
        )
    }

    override val packages: Array<String> = arrayOf("org.radarbase.appconfig.resource")

    override fun AbstractBinder.enhance() {
        // Bind instances. These cannot use any injects themselves
        bind(config)
            .to(ApplicationConfig::class.java)

        bind(ConditionService::class.java)
            .to(ConditionService::class.java)
            .`in`(Singleton::class.java)

        bind(GlobalConfigService::class.java)
            .to(GlobalConfigService::class.java)
            .`in`(Singleton::class.java)

        bind(ProjectConfigServiceImpl::class.java)
            .to(ProjectConfigService::class.java)
            .`in`(Singleton::class.java)

        bind(ClientService::class.java)
            .to(ClientService::class.java)
            .`in`(Singleton::class.java)

        bind(ClientInterpreter::class.java)
            .to(ClientInterpreter::class.java)
            .`in`(Singleton::class.java)

        bind(UserConfigService::class.java)
            .to(UserConfigService::class.java)
            .`in`(Singleton::class.java)

        bind(NonResolvingConfigServiceImpl::class.java)
            .to(NonResolvingConfigService::class.java)
            .`in`(Singleton::class.java)
    }
}
