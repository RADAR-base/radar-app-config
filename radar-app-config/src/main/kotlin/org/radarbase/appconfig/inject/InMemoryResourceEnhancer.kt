package org.radarbase.appconfig.inject

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.lang.expression.DirectVariableResolver
import org.radarbase.lang.expression.VariableResolver
import java.util.concurrent.ConcurrentHashMap

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
