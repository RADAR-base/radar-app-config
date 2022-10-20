package org.radarbase.appconfig.persistence

import com.hazelcast.map.IMap
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.jersey.hibernate.HibernateRepository
import java.util.stream.Stream
import jakarta.inject.Provider
import org.radarbase.lang.expression.*
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import jakarta.persistence.TypedQuery
import kotlin.streams.asSequence

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class HibernateVariableResolver(
    em: Provider<EntityManager>,
    private val clientId: String,
    private val cache: IMap<String, LongArray>,
) : VariableResolver, HibernateRepository(em) {
    override fun replace(
        scope: Scope,
        prefix: QualifiedId?,
        variables: Sequence<Pair<QualifiedId, Variable>>,
    ) = transact {
        deleteConfig(scope, prefix).executeUpdate()

        variables
            .filter { (id, _) -> !id.isEmpty() }
            .forEach { (id, variable) -> save(scope, id, variable) }
        cache -= scope.asString()
    }

    private fun EntityManager.save(
        scope: Scope,
        id: QualifiedId,
        variable: Variable,
    ) {
        require(!id.isEmpty()) { "Cannot save variable without variable name" }

        val configEntity = ConfigEntity().apply {
            this.clientId = this@HibernateVariableResolver.clientId
            this.scope = scope.asString()
            this.name = id.asString()
            this.value = variable.asOptString()
        }
        persist(configEntity)
    }

    override fun register(
        scope: Scope,
        id: QualifiedId,
        variable: Variable,
    ) = transact {
        save(scope, id, variable)
        cache -= scope.asString()
    }

    override fun resolve(
        scopes: List<Scope>,
        id: QualifiedId,
    ): ResolvedVariable {
        require(!id.isEmpty()) { "Cannot get variable without variable name" }
        return transact {
            selectConfigName(scopes, id)
                .resultStream
                .map { it.toResolvedVariable() }
                .reduce(higherScopedVariable(scopes))
                .orElseThrow { NoSuchFieldError("Unknown variable $id in scopes $scopes.") }
        }
    }

    override fun resolveAll(
        scopes: List<Scope>,
        prefix: QualifiedId?,
    ): Sequence<ResolvedVariable> = transact {
        scopes.asSequence()
            .flatMap { selectConfig(it, prefix) }
            .map { find(ConfigEntity::class.java, it).toResolvedVariable() }
            .groupBy {
                if (it.scope == scopes[0]) ActualVariableKey(it.id)
                else DefaultsVariableKey(it.id)
            }
            .values
            .asSequence()
            .map { v -> v.maxByOrNull {
                scopes.indexOf(it.scope)
            }}
            .filterNotNull()
    }

    override fun list(
        scopes: List<Scope>,
        prefix: QualifiedId?,
    ): Sequence<QualifiedId> = transact {
        (listConfig(scopes, prefix).resultStream as Stream<*>)
            .map { rawResult ->
                val result = rawResult as Array<*>
                QualifiedId(result[0] as String, result[1] as String)
            }
            .toList()
            .asSequence()
    }

    private fun EntityManager.deleteConfig(
        scope: Scope,
        prefix: QualifiedId?,
    ): Query = if (prefix == null || prefix.isEmpty()) {
        deleteConfig(scope)
    } else {
        deleteConfig(scope, prefix.asString())
    }

    private fun EntityManager.deleteConfig(
        scope: Scope,
    ): Query = createQuery("DELETE FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId")
        .setParameter("scope", scope.asString())
        .setParameter("clientId", clientId)

    private fun EntityManager.deleteConfig(
        scope: Scope,
        prefix: String,
    ): Query =
        createQuery("DELETE FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId AND c.name LIKE :prefix")
            .setParameter("scope", scope.asString())
            .setParameter("clientId", clientId)
            .setParameter("prefix", "$prefix%")

    private fun EntityManager.selectConfig(
        scope: Scope,
        prefix: QualifiedId?,
    ): Sequence<Long> {
        return if (prefix == null || prefix.isEmpty()) {
            var cachedValue = cache[scope.asString()]
            if (cachedValue == null) {
                cachedValue = selectConfig(scope)
                    .resultList
                    .map { it.toLong() }
                    .toLongArray()
                cache[scope.asString()] = cachedValue
            }
            cachedValue.asSequence()
        } else {
            selectConfigPrefix(scope, prefix.asString())
                .resultStream
                .map { it.toLong() }
                .asSequence()
        }
    }

    private fun EntityManager.selectConfig(
        scope: Scope,
    ): TypedQuery<java.lang.Long> = createQuery(
        "SELECT c.id FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId",
        java.lang.Long::class.java
    )
        .setParameter("scope", scope.asString())
        .setParameter("clientId", clientId)

    private fun EntityManager.selectConfigPrefix(
        scope: Scope,
        prefix: String,
    ): TypedQuery<java.lang.Long> = createQuery(
        "SELECT c.id FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId AND c.name LIKE :prefix",
        java.lang.Long::class.java
    )
        .setParameter("scope", scope.asString())
        .setParameter("clientId", clientId)
        .setParameter("prefix", "$prefix%")

    private fun EntityManager.selectConfigName(
        scopes: List<Scope>,
        name: QualifiedId,
    ): TypedQuery<ConfigEntity> = createQuery(
        "SELECT c FROM Config c WHERE c.scope IN (:scopes) AND c.clientId = :clientId AND c.name = :name",
        ConfigEntity::class.java
    )
        .setParameter("scopes", scopes.map { it.asString() })
        .setParameter("clientId", clientId)
        .setParameter("name", name.asString())

    private fun EntityManager.listConfig(
        scopes: List<Scope>,
        prefix: QualifiedId?,
    ): Query = if (prefix == null || prefix.isEmpty()) {
        listConfig(scopes)
    } else {
        listConfigPrefix(scopes, prefix.asString())
    }

    private fun EntityManager.listConfig(
        scopes: List<Scope>,
    ): TypedQuery<ConfigEntity> = createQuery(
        "SELECT DISTINCT c.clientId, c.name FROM Config c WHERE c.scope IN (:scopes) AND c.clientId = :clientId",
        ConfigEntity::class.java
    )
        .setParameter("scopes", scopes.map { it.asString() })
        .setParameter("clientId", clientId)

    private fun EntityManager.listConfigPrefix(
        scopes: List<Scope>,
        prefix: String,
    ): TypedQuery<ConfigEntity> = createQuery(
        "SELECT DISTINCT c.clientId, c.name FROM Config c WHERE c.scope IN (:scopes) AND c.clientId = :clientId AND c.name LIKE :prefix",
        ConfigEntity::class.java
    )
        .setParameter("scopes", scopes.map { it.asString() })
        .setParameter("clientId", clientId)
        .setParameter("prefix", "$prefix%")

    companion object {
        private fun ConfigEntity.toResolvedVariable() = ResolvedVariable(
            SimpleScope(scope),
            QualifiedId(name),
            value?.toVariable() ?: NullLiteral(),
        )

        private fun higherScopedVariable(scopes: List<Scope>): (ResolvedVariable, ResolvedVariable) -> ResolvedVariable =
            { v1, v2 ->
                if (scopes.indexOf(v1.scope) < scopes.indexOf(v2.scope)) v1 else v2
            }
    }
}

sealed class VariableKey {
    abstract val id: QualifiedId
}

data class DefaultsVariableKey(override val id: QualifiedId) : VariableKey()
data class ActualVariableKey(override val id: QualifiedId) : VariableKey()
