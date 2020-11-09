package org.radarbase.appconfig.inject

import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import nl.thehyve.lang.expression.VariableResolver
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.appconfig.persistence.HibernateVariableResolver
import org.radarbase.jersey.config.JerseyResourceEnhancer
import java.util.function.Supplier
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.ws.rs.core.Context


class HibernatePersistenceResourceEnhancer : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        bindFactory(HazelcastInstanceFactory::class.java)
            .to(HazelcastInstance::class.java)
            .`in`(Singleton::class.java)

        bind(HibernateClientVariableResolver::class.java)
                .to(ClientVariableResolver::class.java)
                .`in`(Singleton::class.java)
    }

    class HibernateClientVariableResolver(
        @Context private val em: javax.inject.Provider<EntityManager>,
        @Context private val hazelcastInstance: HazelcastInstance,
    ): ClientVariableResolver {
        override fun get(clientId: String): VariableResolver = HibernateVariableResolver(em, clientId, hazelcastInstance.getMap(clientId))
    }

    class HazelcastInstanceFactory: Supplier<HazelcastInstance> {
        override fun get(): HazelcastInstance = Hazelcast.getAllHazelcastInstances()
            .first { it.config.clusterName == "appconfig" }
    }
}
