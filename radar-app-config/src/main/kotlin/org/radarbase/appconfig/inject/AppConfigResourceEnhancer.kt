package org.radarbase.appconfig.inject

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nl.thehyve.lang.expression.*
import nl.thehyve.lang.expression.Function
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.internal.inject.PerThread
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.appconfig.Config
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConditionService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.appconfig.service.ProjectService
import org.radarbase.jersey.config.JerseyResourceEnhancer
import javax.inject.Singleton
import javax.ws.rs.ext.ContextResolver

class AppConfigResourceEnhancer(private val config: Config): JerseyResourceEnhancer {
    private val mapper = createMapper()

    override fun enhanceResources(resourceConfig: ResourceConfig) {
        resourceConfig.packages("org.radarbase.appconfig.resource")
        resourceConfig.register(ContextResolver { mapper })
    }

    override fun enhanceBinder(binder: AbstractBinder) {
        binder.apply {
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
            bind(MPClient::class.java)
                    .to(MPClient::class.java)
                    .`in`(PerThread::class.java)

            bind(mapper)
                    .to(ObjectMapper::class.java)
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