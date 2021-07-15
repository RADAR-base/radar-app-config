package org.radarbase.appconfig.inject

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.jersey.config.JerseyResourceEnhancer
import java.util.concurrent.ConcurrentHashMap
import jakarta.inject.Singleton
import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.persistence.ClientVariableSet
import org.radarbase.appconfig.persistence.ConfigRepository

class InMemoryResourceEnhancer : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        // Bind factories.
        bind(InMemoryConfigRepository::class.java)
            .to(ConfigRepository::class.java)
            .`in`(Singleton::class.java)
    }

    class InMemoryConfigRepository : ConfigRepository {
        private val resolvers = ConcurrentHashMap<String, VariableResolver>()

        private fun clientResolver(clientId: String) = resolvers.computeIfAbsent(clientId) {
            DirectVariableResolver()
        }

        override fun update(clientId: String, variableSet: VariableSet): UpdateResult {
            return clientResolver(clientId).update(variableSet)
        }

        override fun findActive(clientId: String, scope: Scope): VariableSet? {
            return clientResolver(clientId).resolve(scope)
        }

        override fun get(id: Long): ClientVariableSet? {
            return resolvers.entries.asSequence()
                .mapNotNull { (clientId, resolver) ->
                    val variableResolver = resolver.get(id)
                    if (variableResolver != null) ClientVariableSet(clientId, variableResolver) else null
                }
                .firstOrNull()
        }

        override fun toString(): String {
            return "InMemoryConfigRepository(resolvers=$resolvers)"
        }
    }
}
