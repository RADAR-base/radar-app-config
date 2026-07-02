package org.radarbase.appconfig.inject

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.appconfig.config.HazelcastConfig
import org.radarbase.appconfig.persistence.HibernateVariableRepository
import org.radarbase.appconfig.persistence.HibernateVariableResolver
import org.radarbase.appconfig.persistence.VariableRepository
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.lang.expression.VariableResolver

class HibernatePersistenceResourceEnhancer(
    private val hazelcastConfig: HazelcastConfig,
) : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {

        bind(HibernateClientVariableResolver::class.java)
            .to(ClientVariableResolver::class.java)
            .`in`(Singleton::class.java)

        bind(HibernateClientVariableRepository::class.java)
            .to(ClientVariableRepository::class.java)
            .`in`(Singleton::class.java)

        if (hazelcastConfig.enable) {
            System.setProperty("hazelcast.logging.type", "slf4j")
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
        }
    }

    class HibernateClientVariableResolver(
        @Context private val em: jakarta.inject.Provider<EntityManager>,
        @Context private val hazelcastInstance: HazelcastInstance,
        @Context private val asyncService: AsyncCoroutineService,
    ) : ClientVariableResolver {
        override fun get(clientId: String): VariableResolver = HibernateVariableResolver(
            em,
            clientId,
            hazelcastInstance.getMap(clientId),
            asyncService,
            this.hazelcastInstance.name, // this gets the name of the user identity name configured in management portal
        )
    }

    // TODO I do not understand the caching mechanism as used in HibernateClientVariableResolver
    // Investigate and add if needed.
    class HibernateClientVariableRepository(
        @Context private val em: jakarta.inject.Provider<EntityManager>,
        @Context private val asyncService: AsyncCoroutineService,
    ) : ClientVariableRepository {
        override fun get(clientId: String): VariableRepository = HibernateVariableRepository(
            em,
            clientId,
            asyncService,
        )
    }
}
