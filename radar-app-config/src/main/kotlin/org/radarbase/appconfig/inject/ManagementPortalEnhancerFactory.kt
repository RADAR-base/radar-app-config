package org.radarbase.appconfig.inject

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.*
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.appconfig.persistence.entity.ConfigStateEntity
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.config.EnhancerFactory
import org.radarbase.jersey.config.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.config.HibernateResourceEnhancer
import org.radarbase.lang.expression.*
import org.radarbase.lang.expression.Function
import kotlin.reflect.jvm.jvmName

/** This binder needs to register all non-Jersey classes, otherwise initialization fails. */
class ManagementPortalEnhancerFactory(private val config: ApplicationConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> {
        val resolverEnhancer = if (config.database != null) {
            val databaseConfig = config.database.copy(
                managedClasses = listOf(
                    ConfigEntity::class.jvmName,
                    ConfigStateEntity::class.jvmName,
                ),
                properties = mapOf(
                    "hibernate.cache.use_second_level_cache" to "true",
                    "hibernate.cache.region.factory_class" to "com.hazelcast.hibernate.HazelcastLocalCacheRegionFactory",
                    "hibernate.cache.hazelcast.instance_name" to config.hazelcast.instanceName,
                ) + config.database.properties,
            )
            listOf(
                HibernateResourceEnhancer(databaseConfig),
                HibernatePersistenceResourceEnhancer(config.hazelcast),
            )
        } else {
            listOf(InMemoryResourceEnhancer())
        }

        val radarEnhancer = ConfigLoader.Enhancers.radar(config.auth).apply {
            utilityResourceEnhancer = null
        }
        val utility = ConfigLoader.Enhancers.utility.apply {
            mapper = jsonMapper {
                serializationInclusion(JsonInclude.Include.NON_NULL)
                addModule(JavaTimeModule())
                addModule(kotlinModule {
                    nullIsSameAsDefault(true)
                    nullToEmptyCollection(true)
                    nullToEmptyMap(true)
                })
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                addModule(SimpleModule().apply {
                    val allowedFunctions = listOf<Function>(
                        SumFunction(),
//                        ListVariablesFunction(),
                        CountFunction()
                    )
                    val deserializer = ExpressionDeserializer(ExpressionParser(allowedFunctions))

                    addDeserializer(Expression::class, deserializer)
                })
            }
        }

        val authEnhancer = if (config.isAuthEnabled) {
            ConfigLoader.Enhancers.managementPortal(config.auth)
        } else ConfigLoader.Enhancers.disabledAuthorization

        return listOf(
            utility,
            AppConfigResourceEnhancer(config),
            radarEnhancer,
            ConfigLoader.Enhancers.health,
            ConfigLoader.Enhancers.generalException,
            ConfigLoader.Enhancers.httpException,
        ) + resolverEnhancer + authEnhancer
    }
}
