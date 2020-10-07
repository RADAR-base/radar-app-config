package org.radarbase.appconfig.inject

import nl.thehyve.lang.expression.VariableResolver
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.appconfig.persistence.HibernateVariableResolver
import org.radarbase.jersey.config.JerseyResourceEnhancer
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.ws.rs.core.Context

class HibernatePersistenceResourceEnhancer : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        bind(HibernateClientVariableResolver::class.java)
                .to(ClientVariableResolver::class.java)
                .`in`(Singleton::class.java)
    }

    class HibernateClientVariableResolver(
            @Context private val em: javax.inject.Provider<EntityManager>
    ): ClientVariableResolver {
        override fun get(clientId: String): VariableResolver = HibernateVariableResolver(em, clientId)
    }
}
