package org.radarbase.appconfig.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Provider
import jakarta.ws.rs.core.Context
import nl.thehyve.lang.expression.Scope
import nl.thehyve.lang.expression.SimpleScope
import org.radarbase.appconfig.domain.ClientProtocol
import org.radarbase.appconfig.persistence.entity.ProtocolEntity
import org.radarbase.jersey.exception.HttpConflictException
import org.radarbase.jersey.hibernate.HibernateRepository
import javax.persistence.EntityManager
import javax.persistence.NoResultException

class ProtocolRepository(
    @Context em: Provider<EntityManager>,
    @Context private val mapper: ObjectMapper,
) : HibernateRepository(em) {
    fun protocol(clientId: String, scope: Scope): ProtocolEntity = transact {
        retrieveProtocol(clientId, scope)
    }

    private fun EntityManager.retrieveProtocol(clientId: String, scope: Scope): ProtocolEntity {
        return createQuery("SELECT p FROM Protocol p WHERE p.clientId = :clientId AND p.scope = :scope", ProtocolEntity::class.java)
            .setParameter("clientId", clientId)
            .setParameter("scope", scope.asString())
            .singleResult
    }

    fun store(clientProtocol: ClientProtocol): Boolean = transact {
        val providedScope = requireNotNull(clientProtocol.scope) { "Scope must be set to persist "}
        val scope = SimpleScope(providedScope)
        val contents = mapper.writeValueAsString(clientProtocol.contents)
        return@transact try {
            val existingProtocol = retrieveProtocol(clientProtocol.clientId, scope)
            existingProtocol.contents = contents
            existingProtocol.lastModifiedAt = clientProtocol.lastModifiedAt
            existingProtocol.version = clientProtocol.version
            merge(existingProtocol)
            true
        } catch (ex: NoResultException) {
            val newProtocol = ProtocolEntity().apply {
                this.contents = contents
                this.scope = providedScope
                lastModifiedAt = clientProtocol.lastModifiedAt
                clientId = clientProtocol.clientId
                version = clientProtocol.version
            }
            persist(newProtocol)
            false
        }
    }
}
