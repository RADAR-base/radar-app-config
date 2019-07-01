package org.radarbase.appconfig.inject

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.process.internal.RequestScoped
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.appconfig.Config
import org.radarbase.appconfig.auth.Auth

interface AppConfigResources {
    fun resources(config: Config): ResourceConfig {
        val resources = ResourceConfig()
        resources.packages(
                "org.radarbase.appconfig.auth",
                "org.radarbase.appconfig.exception",
                "org.radarbase.appconfig.filter",
                "org.radarbase.appconfig.io",
                "org.radarbase.appconfig.resource")

        resources.register(getBinder(config))
        resources.property("jersey.config.server.wadl.disableWadl", true)
        registerAuthentication(resources)
        return resources
    }

    fun registerAuthentication(resources: ResourceConfig)

    fun registerAuthenticationUtilities(binder: AbstractBinder)

    fun getBinder(config: Config) = object : AbstractBinder() {
        override fun configure() {
            // Bind instances. These cannot use any injects themselves
            bind(config)
                    .to(Config::class.java)

            // Bind factories.
            bindFactory(AuthFactory::class.java)
                    .proxy(true)
                    .proxyForSameScope(false)
                    .to(Auth::class.java)
                    .`in`(RequestScoped::class.java)

            registerAuthenticationUtilities(this)
        }
    }
}
