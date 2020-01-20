package org.radarbase.appconfig.inject

import nl.thehyve.lang.expression.DirectVariableResolver
import nl.thehyve.lang.expression.VariableResolver
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.process.internal.RequestScoped
import org.radarbase.jersey.config.JerseyResourceEnhancer
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.ws.rs.core.Context

class InMemoryResourceEnhancer : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        // Bind factories.
        bind(InMemoryClientVariableResolver::class.java)
                .to(ClientVariableResolver::class.java)
                .`in`(Singleton::class.java)
    }

    class InMemoryClientVariableResolver(
            @Context private val em: EntityManager
    ) : ClientVariableResolver {
        private val resolvers = ConcurrentHashMap<String, VariableResolver>()
        override fun get(clientId: String) = resolvers.computeIfAbsent(clientId) { DirectVariableResolver() }
    }
}
