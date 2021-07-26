package org.radarbase.appconfig.inject

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.jersey.config.JerseyResourceEnhancer
import java.util.concurrent.ConcurrentHashMap
import jakarta.inject.Singleton
import org.radarbase.appconfig.persistence.ClientVariableSet
import org.radarbase.appconfig.persistence.ConfigRepository
import org.radarbase.lang.expression.*

class InMemoryResourceEnhancer : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        // Bind factories.
        bind(InMemoryConfigRepository::class.java)
            .to(ConfigRepository::class.java)
            .`in`(Singleton::class.java)
    }

    class InMemoryConfigRepository : ConfigRepository {
        private val resolvers = ConcurrentHashMap<String, VariableRepository>()

        private fun clientResolver(clientId: String) = resolvers.computeIfAbsent(clientId) {
            DirectVariableRepository()
        }

        override fun update(clientId: String, variableSet: VariableSet): UpdateResult {
            return clientResolver(clientId).update(variableSet)
        }

        override fun findActiveValue(clientId: String, scopes: List<Scope>, id: QualifiedId): ResolvedVariable? {
            return try {
                clientResolver(clientId).resolve(scopes, id)
            } catch (ex: NoSuchElementException) {
                null
            }
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
