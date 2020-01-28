package org.radarbase.appconfig.persistence

import nl.thehyve.lang.expression.*
import org.hibernate.criterion.MatchMode
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery

class HibernateVariableResolver(
        private val em: EntityManager,
        private val clientId: String
): VariableResolver {
    override fun replace(scope: Scope, prefix: QualifiedId?, variables: Stream<Pair<QualifiedId, Variable>>) = em.run {
        val query = deleteConfig(scope, prefix)

        transact {
            query.executeUpdate()

            variables
                    .filter { (id, _) -> !id.isEmpty() }
                    .forEach { (id, variable) -> save(scope, id, variable) }
        }
    }

    private fun EntityManager.save(scope: Scope, id: QualifiedId, variable: Variable) {
        if (id.isEmpty()) {
            throw IllegalArgumentException("Cannot save variable without variable name")
        }

        val configEntity = ConfigEntity().apply {
            this.clientId = this@HibernateVariableResolver.clientId
            this.scope = scope.asString()
            this.name = id.asString()
            this.value = variable.asOptString()
        }
        persist(configEntity)
    }

    override fun register(scope: Scope, id: QualifiedId, variable: Variable) = em.transact {
        save(scope, id, variable)
    }

    override fun resolve(scopes: List<Scope>, id: QualifiedId): ResolvedVariable {
        if (id.isEmpty()) {
            throw IllegalArgumentException("Cannot get variable without variable name")
        }

        return em.run {
            val query = selectConfigName(scopes, id)

            transact {
                query.resultStream
                        .map { it.toResolvedVariable() }
                        .reduce(higherScopedVariable(scopes))
                        .orElseThrow { NoSuchFieldError("Unknown variable $id in scopes $scopes.") }
            }
        }
    }

    override fun resolveAll(scopes: List<Scope>, prefix: QualifiedId?): Stream<ResolvedVariable> = em.run {
        val query = selectConfig(scopes, prefix)

        transact {
            query.resultStream
                    .map { it.toResolvedVariable() }
                    .collect(Collectors.toMap<ResolvedVariable, VariableKey, ResolvedVariable>({
                        if (it.scope == scopes[0]) ActualVariableKey(it.id)
                        else DefaultsVariableKey(it.id)
                    }, { it }, higherScopedVariable(scopes)))
                    .values
                    .stream()
        }
    }

    override fun list(scopes: List<Scope>, prefix: QualifiedId?): Stream<QualifiedId> = em.run {
        val query = listConfig(scopes, prefix)

        transact {
            query.resultStream
                    .map { rawResult ->
                        val result = rawResult as Array<*>
                        QualifiedId(result[0] as String, result[1] as String)
                    }
                    .collect(Collectors.toList())
                    .stream()
        }
    }

    private fun EntityManager.deleteConfig(scope: Scope, prefix: QualifiedId?): Query {
        return if (prefix == null || prefix.isEmpty()) deleteConfig(scope)
        else deleteConfig(scope, prefix.asString())
    }

    private fun EntityManager.deleteConfig(scope: Scope): Query {
        return createQuery("DELETE FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId")
                .setParameter("scope", scope.asString())
                .setParameter("clientId", clientId)
    }

    private fun EntityManager.deleteConfig(scope: Scope, prefix: String): Query {
        return createQuery("DELETE FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId AND c.name LIKE :prefix")
                .setParameter("scope", scope.asString())
                .setParameter("clientId", clientId)
                .setParameter("prefix", MatchMode.START.toMatchString(prefix))
    }

    private fun EntityManager.selectConfig(scopes: List<Scope>, prefix: QualifiedId?): TypedQuery<ConfigEntity> {
        return if (prefix == null || prefix.isEmpty()) selectConfig(scopes)
        else selectConfigPrefix(scopes, prefix.asString())
    }

    private fun EntityManager.selectConfig(scopes: List<Scope>): TypedQuery<ConfigEntity> {
        return createQuery("SELECT c FROM Config c WHERE c.scope IN (:scopes) AND c.clientId = :clientId", ConfigEntity::class.java)
                .setParameter("scopes", scopes.map { it.asString() })
                .setParameter("clientId", clientId)
    }

    private fun EntityManager.selectConfigPrefix(scopes: List<Scope>, prefix: String): TypedQuery<ConfigEntity> {
        return createQuery("SELECT c FROM Config c WHERE c.scope IN (:scopes) AND c.clientId = :clientId AND c.name LIKE :prefix", ConfigEntity::class.java)
                .setParameter("scopes", scopes.map { it.asString() })
                .setParameter("clientId", clientId)
                .setParameter("prefix", MatchMode.START.toMatchString(prefix))
    }

    private fun EntityManager.selectConfigName(scopes: List<Scope>, name: QualifiedId): TypedQuery<ConfigEntity> {
        return createQuery("SELECT c FROM Config c WHERE c.scope IN (:scopes) AND c.clientId = :clientId AND c.name = :name", ConfigEntity::class.java)
                .setParameter("scopes", scopes.map { it.asString() })
                .setParameter("clientId", clientId)
                .setParameter("name", name.asString())
    }

    private fun EntityManager.listConfig(scopes: List<Scope>, prefix: QualifiedId?): Query {
        return if (prefix == null || prefix.isEmpty()) listConfig(scopes)
        else listConfigPrefix(scopes, prefix.asString())
    }

    private fun EntityManager.listConfig(scopes: List<Scope>): TypedQuery<ConfigEntity> {
        return createQuery("SELECT DISTINCT c.clientId, c.name FROM Config c WHERE c.scope IN (:scopes) AND c.clientId = :clientId", ConfigEntity::class.java)
                .setParameter("scopes", scopes.map { it.asString() })
                .setParameter("clientId", clientId)
    }

    private fun EntityManager.listConfigPrefix(scopes: List<Scope>, prefix: String): TypedQuery<ConfigEntity> {
        return createQuery("SELECT DISTINCT c.clientId, c.name FROM Config c WHERE c.scope IN (:scopes) AND c.clientId = :clientId AND c.name LIKE :prefix", ConfigEntity::class.java)
                .setParameter("scopes", scopes.map { it.asString() })
                .setParameter("clientId", clientId)
                .setParameter("prefix", MatchMode.START.toMatchString(prefix))
    }

    companion object {
        private fun ConfigEntity.toResolvedVariable() = ResolvedVariable(
                SimpleScope(scope),
                QualifiedId(name),
                value?.toVariable() ?: NullLiteral())

        private fun higherScopedVariable(scopes: List<Scope>): (ResolvedVariable, ResolvedVariable) -> ResolvedVariable = { v1, v2 ->
            if (scopes.indexOf(v1.scope) < scopes.indexOf(v2.scope)) v1 else v2
        }
    }

}

sealed class VariableKey {
    abstract val id: QualifiedId
}

data class DefaultsVariableKey(override val id: QualifiedId): VariableKey()
data class ActualVariableKey(override val id: QualifiedId): VariableKey()
