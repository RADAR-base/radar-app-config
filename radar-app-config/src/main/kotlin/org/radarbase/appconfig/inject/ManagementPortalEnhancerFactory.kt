package org.radarbase.appconfig.inject

import com.fasterxml.jackson.databind.module.SimpleModule
import nl.thehyve.lang.expression.*
import nl.thehyve.lang.expression.Function
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.config.EnhancerFactory
import org.radarbase.jersey.config.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.config.HibernateResourceEnhancer

/** This binder needs to register all non-Jersey classes, otherwise initialization fails. */
class ManagementPortalEnhancerFactory(private val config: ApplicationConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> {
        val resolverEnhancer = if (config.database != null) {
            listOf(
                    HibernateResourceEnhancer(config.database.copy(
                            managedClasses = listOf(ConfigEntity::class.qualifiedName!!)
                    )),
                    HibernatePersistenceResourceEnhancer())
        } else {
            listOf(InMemoryResourceEnhancer())
        }

        val radarEnhancer = ConfigLoader.Enhancers.radar(config.auth)
        radarEnhancer.mapper.registerModule(SimpleModule().apply {
            val allowedFunctions = listOf<Function>(
                    SumFunction(),
                    ListVariablesFunction(),
                    CountFunction()
            )
            val deserializer = ExpressionDeserializer(ExpressionParser(allowedFunctions))

            addDeserializer(Expression::class.java, deserializer)
        })

        return listOf(
                AppConfigResourceEnhancer(config),
                radarEnhancer,
                ConfigLoader.Enhancers.managementPortal(config.auth),
                ConfigLoader.Enhancers.health,
                ConfigLoader.Enhancers.generalException,
                ConfigLoader.Enhancers.httpException) + resolverEnhancer
    }
}
