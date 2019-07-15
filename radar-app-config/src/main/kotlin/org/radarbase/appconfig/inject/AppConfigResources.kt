package org.radarbase.appconfig.inject

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nl.thehyve.lang.expression.*
import nl.thehyve.lang.expression.Function
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.internal.inject.PerThread
import org.glassfish.jersey.process.internal.RequestScoped
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.appconfig.Config
import org.radarbase.appconfig.auth.Auth
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConditionService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.appconfig.service.ProjectService
import javax.inject.Singleton
import javax.ws.rs.ext.ContextResolver

interface AppConfigResources {
    fun resources(config: Config): ResourceConfig {
        val resources = ResourceConfig()
        resources.packages(
                "org.radarbase.appconfig.auth",
                "org.radarbase.appconfig.exception",
                "org.radarbase.appconfig.filter",
                "org.radarbase.appconfig.io",
                "org.radarbase.appconfig.resource")

        val mapper = createMapper()

        resources.register(getBinder(config, mapper))
        resources.property("jersey.config.server.wadl.disableWadl", true)
        resources.register(ContextResolver { mapper })
        registerAuthentication(resources)
        return resources
    }

    fun registerAuthentication(resources: ResourceConfig)

    fun registerAuthenticationUtilities(binder: AbstractBinder)

    fun getBinder(config: Config, mapper: ObjectMapper) = object : AbstractBinder() {
        override fun configure() {
            // Bind instances. These cannot use any injects themselves
            bind(config)
                    .to(Config::class.java)

            bind(ConditionService::class.java)
                    .to(ConditionService::class.java)
                    .`in`(Singleton::class.java)

            bind(ConfigService::class.java)
                    .to(ConfigService::class.java)
                    .`in`(Singleton::class.java)

            bind(ProjectService::class.java)
                    .to(ProjectService::class.java)
                    .`in`(Singleton::class.java)

            bind(ClientService::class.java)
                    .to(ClientService::class.java)
                    .`in`(Singleton::class.java)

            val variableResolver = DirectVariableResolver()

            bind(variableResolver)
                    .to(VariableResolver::class.java)

            bind(Interpreter(variableResolver))
                    .to(Interpreter::class.java)

            // Bind factories.
            bindFactory(AuthFactory::class.java)
                    .proxy(true)
                    .proxyForSameScope(false)
                    .to(Auth::class.java)
                    .`in`(RequestScoped::class.java)

            bind(MPClient::class.java)
                    .to(MPClient::class.java)
                    .`in`(PerThread::class.java)

            bind(mapper)
                    .to(ObjectMapper::class.java)

            registerAuthenticationUtilities(this)
        }
    }

    companion object {
        fun createMapper(): ObjectMapper {
            val allowedFunctions = listOf<Function>(
                    SumFunction(),
                    ListVariablesFunction(),
                    CountFunction()
            )
            val deserializer = ExpressionDeserializer(ExpressionParser(allowedFunctions))

            return jacksonObjectMapper()
                    .registerModule(SimpleModule().apply {
                        addDeserializer(Expression::class.java, deserializer)
                    })
        }

    }
}
