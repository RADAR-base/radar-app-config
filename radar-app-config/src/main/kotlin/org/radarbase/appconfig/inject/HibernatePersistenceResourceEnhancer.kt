package org.radarbase.appconfig.inject

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import nl.thehyve.lang.expression.VariableResolver
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.appconfig.config.HazelcastConfig
import org.radarbase.appconfig.persistence.HibernateVariableResolver
import org.radarbase.jersey.config.JerseyResourceEnhancer
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.ws.rs.core.Context

class HibernatePersistenceResourceEnhancer(private val hazelcastConfig: HazelcastConfig) : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        System.setProperty("hazelcast.logging.type", "slf4j");
        val hzConfig = if (hazelcastConfig.configPath != null) {
            com.hazelcast.internal.config.ConfigLoader.load(hazelcastConfig.configPath)
        } else {
            Config().apply {
                networkConfig = hazelcastConfig.network
            }
        }.apply {
            clusterName = hazelcastConfig.clusterName
            instanceName = hazelcastConfig.instanceName
        }

        val hazelcastInstance = Hazelcast.newHazelcastInstance(hzConfig)

        bind(hazelcastInstance)
            .to(HazelcastInstance::class.java)
            .`in`(Singleton::class.java)

        bind(HibernateClientVariableResolver::class.java)
            .to(ClientVariableResolver::class.java)
            .`in`(Singleton::class.java)
    }

    class HibernateClientVariableResolver(
        @Context private val em: javax.inject.Provider<EntityManager>,
        @Context private val hazelcastInstance: HazelcastInstance,
    ) : ClientVariableResolver {
        override fun get(clientId: String): VariableResolver =
            HibernateVariableResolver(em, clientId, hazelcastInstance.getMap(clientId))
    }
}
