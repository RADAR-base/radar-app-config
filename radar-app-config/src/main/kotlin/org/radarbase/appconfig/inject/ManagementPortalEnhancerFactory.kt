package org.radarbase.appconfig.inject

import com.fasterxml.jackson.databind.module.SimpleModule
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.enhancer.MapperResourceEnhancer
import org.radarbase.jersey.hibernate.config.HibernateResourceEnhancer
import org.radarbase.lang.expression.*
import org.radarbase.lang.expression.Function

/** This binder needs to register all non-Jersey classes, otherwise initialization fails. */
class ManagementPortalEnhancerFactory(private val config: ApplicationConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> = buildList {
        if (config.database != null) {
            val databaseConfig = config.database.copy(
                managedClasses = listOf(ConfigEntity::class.qualifiedName!!),
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

        add(MapperResourceEnhancer().apply {
            mapper = MapperResourceEnhancer.createDefaultMapper()
                .registerModule(SimpleModule().apply {
                    val allowedFunctions = listOf<Function>(
                        SumFunction(),
                        ListVariablesFunction(),
                        CountFunction(),
                    )
                    addDeserializer(
                        Expression::class.java,
                        ExpressionDeserializer(ExpressionParser(allowedFunctions)),
                    )
                })
        })

        add(AppConfigResourceEnhancer(config))
        add(Enhancers.radar(config.auth, includeMapper = false))
        add(Enhancers.managementPortal(config.auth))
        add(Enhancers.health)
        add(Enhancers.exception)
    }
}
