package org.radarbase.appconfig.inject

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import kotlin.reflect.jvm.jvmName
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.appconfig.persistence.entity.ConditionEntity
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.appconfig.persistence.entity.ConfigStateEntity
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.config.HibernateResourceEnhancer
import org.radarbase.lang.expression.*

/** This binder needs to register all non-Jersey classes, otherwise initialization fails. */
class ManagementPortalEnhancerFactory(private val config: ApplicationConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> = buildList {
        if (config.database != null) {
            val databaseConfig = config.database.copy(
                managedClasses = listOf(
                    ConfigEntity::class.jvmName,
                    ConfigStateEntity::class.jvmName,
                    ConditionEntity::class.jvmName,
                ),
                properties = mapOf(
                    "hibernate.cache.use_second_level_cache" to "true",
                    "hibernate.cache.region.factory_class" to "com.hazelcast.hibernate.HazelcastLocalCacheRegionFactory",
                    "hibernate.cache.hazelcast.instance_name" to config.hazelcast.instanceName,
                ) + config.database.properties,
            )
            add(HibernateResourceEnhancer(databaseConfig))
            add(HibernatePersistenceResourceEnhancer(config.hazelcast))
        } else {
            add(InMemoryResourceEnhancer())
        }

        add(Enhancers.radar(config.auth, includeMapper = false))
        val allowedFunctions = listOf(
            SumFunction(),
            CountFunction(),
        )
        add(
            Enhancers.mapper.apply {
                mapper = jsonMapper {
                    serializationInclusion(JsonInclude.Include.NON_NULL)
                    addModule(JavaTimeModule())
                    addModule(
                        kotlinModule {
                            enable(KotlinFeature.NullIsSameAsDefault)
                            enable(KotlinFeature.NullToEmptyCollection)
                            enable(KotlinFeature.NullToEmptyMap)
                        }
                    )
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    addModule(
                        SimpleModule().apply {
                            addDeserializer(
                                Expression::class.java,
                                ExpressionDeserializer(ExpressionParser(allowedFunctions)),
                            )
                            addSerializer(
                                Expression::class.java,
                                ExpressionSerializer(),
                            )
                        }
                    )
                }
            }
        )

        if (config.isAuthEnabled) {
            add(Enhancers.managementPortal(config.auth))
        } else {
            add(Enhancers.disabledAuthorization)
        }

        add(AppConfigResourceEnhancer(config, allowedFunctions))
        add(Enhancers.health)
        add(Enhancers.exception)
    }
}
