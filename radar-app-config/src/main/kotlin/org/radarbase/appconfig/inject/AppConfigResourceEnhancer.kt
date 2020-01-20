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
import org.radarbase.appconfig.service.*
import org.radarbase.jersey.auth.ProjectService
import org.radarbase.jersey.config.JerseyResourceEnhancer
import javax.inject.Singleton
import javax.ws.rs.ext.ContextResolver

class AppConfigResourceEnhancer(private val config: Config): JerseyResourceEnhancer {
    private val mapper = createMapper()

    override val packages: Array<String> = arrayOf("org.radarbase.appconfig.resource")

    override fun ResourceConfig.enhance() {
        register(ContextResolver { mapper })
    }

    override fun AbstractBinder.enhance() {
        // Bind instances. These cannot use any injects themselves
        bind(config)
                .to(Config::class.java)

        bind(ConditionService::class.java)
                .to(ConditionService::class.java)
                .`in`(Singleton::class.java)

        bind(ConfigService::class.java)
                .to(ConfigService::class.java)
                .`in`(Singleton::class.java)

        bind(MPProjectService::class.java)
                .to(ConfigProjectService::class.java)
                .`in`(Singleton::class.java)

        bind(ProjectAuthService::class.java)
                .to(ProjectService::class.java)
                .`in`(Singleton::class.java)

        bind(ClientService::class.java)
                .to(ClientService::class.java)
                .`in`(Singleton::class.java)

        bind(ClientInterpreter::class.java)
                .to(ClientInterpreter::class.java)
                .`in`(Singleton::class.java)

        bind(MPClient::class.java)
                .to(MPClient::class.java)
                .`in`(PerThread::class.java)

        bind(mapper)
                .to(ObjectMapper::class.java)
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