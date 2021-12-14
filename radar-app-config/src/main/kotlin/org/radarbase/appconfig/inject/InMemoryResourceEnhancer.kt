package org.radarbase.appconfig.inject

import nl.thehyve.lang.expression.DirectVariableResolver
import nl.thehyve.lang.expression.VariableResolver
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import java.util.concurrent.ConcurrentHashMap
import jakarta.inject.Singleton

class InMemoryResourceEnhancer : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        // Bind factories.
        bind(InMemoryClientVariableResolver::class.java)
            .to(ClientVariableResolver::class.java)
            .`in`(Singleton::class.java)
    }

    class InMemoryClientVariableResolver : ClientVariableResolver {
        private val resolvers = ConcurrentHashMap<String, VariableResolver>()
        override fun get(clientId: String) = resolvers.computeIfAbsent(clientId) {
            DirectVariableResolver()
        }
    }
}
