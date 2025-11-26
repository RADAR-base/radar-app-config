package org.radarbase.appconfig.persistence

import com.hazelcast.map.IMap
import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import jakarta.persistence.TypedQuery
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.lang.expression.NullLiteral
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.ResolvedVariable
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.SimpleScope
import org.radarbase.lang.expression.Variable
import org.radarbase.lang.expression.VariableResolver
import org.radarbase.lang.expression.toVariable
import org.slf4j.LoggerFactory
import java.util.stream.Stream

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class HibernateVariableResolver(
    em: Provider<EntityManager>,
    private val clientId: String,
    private val cache: IMap<String, LongArray>,
    asyncCoroutineService: AsyncCoroutineService,
    private val createdByUsername: String?,
) : VariableResolver, HibernateRepository(em, asyncCoroutineService) {
    private val logger = LoggerFactory.getLogger(HibernateVariableResolver::class.java)
    override suspend fun replace(
        scope: Scope,
        prefix: QualifiedId?,
        variables: Sequence<Pair<QualifiedId, Variable>>,
    ) = transact {
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

        // Determine the next version based on the most recent existing config
        val nextVersion = ((selectMaxVersion(scope, id) ?: 0)) + 1

        val configEntity = ConfigEntity().apply {
            this.clientId = this@HibernateVariableResolver.clientId
            this.scope = scope.asString()
            this.name = id.asString()
            this.value = variable.asOptString()
            this.createdByUser = createdByUsername
            this.version = nextVersion

        }
        // Log creator info and basic identifiers at info level on creation
        logger.info(
            "Created config entry: clientId={} scope={} name={} createdBy={}",
            configEntity.clientId,
            configEntity.scope,
            configEntity.name,
            configEntity.createdByUser,
        )
        persist(configEntity)
    }

    override suspend fun register(
        scope: Scope,
        id: QualifiedId,
        variable: Variable,
    ) = transact {
        save(scope, id, variable)
        cache -= scope.asString()
    }

    override suspend fun resolve(
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

    override suspend fun resolveVersions(
        scopes: List<Scope>,
        id: QualifiedId,
    ): Sequence<ResolvedVariable> = transact {
        val scope = scopes.firstOrNull() ?: return@transact emptySequence()
        selectConfigVersions(scope, id)
            .resultList
            .asSequence()
            .map { it.toResolvedVariable() }
    }

    override suspend fun resolveVersion(
        scopes: List<Scope>,
        id: QualifiedId,
        version: Int,
    ): Sequence<ResolvedVariable> = transact {
        val scope = scopes.firstOrNull() ?: return@transact emptySequence()
        selectConfigVersion(scope, id, version)
            .resultList
            .asSequence()
            .map { it.toResolvedVariable() }
    }

    override suspend fun resolveAll(
        scopes: List<Scope>,
        prefix: QualifiedId?,
    ): Sequence<ResolvedVariable> = transact {
        scopes.asSequence()
            .flatMap { selectConfig(it, prefix).asSequence() }
            .distinct()
            .map { find(ConfigEntity::class.java, it).toResolvedVariable() }
            .groupBy {
                if (it.scope == scopes[0]) {
                    ActualVariableKey(it.id)
                } else {
                    DefaultsVariableKey(it.id)
                }
            }
            .values
            .asSequence()
            .mapNotNull { v -> v.minByOrNull { scopes.indexOf(it.scope) } }
    }

    override suspend fun list(
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
    ): LongArray {
        return if (prefix == null || prefix.isEmpty()) {
            cache.computeIfAbsent(scope.asString()) {
                selectConfig(scope)
            }
        } else {
            selectConfigPrefix(scope, prefix.asString())
        }
    }

    private fun EntityManager.selectConfig(
        scope: Scope,
    ): LongArray = createQuery(
        "SELECT c.id FROM Config c " +
            "WHERE c.scope = :scope AND c.clientId = :clientId " +
            "AND c.version = (" +
            "  SELECT max(c2.version) FROM Config c2 " +
            "  WHERE c2.scope = c.scope AND c2.clientId = c.clientId AND c2.name = c.name" +
            ")",
        java.lang.Long::class.java,
    )
        .setParameter("scope", scope.asString())
        .setParameter("clientId", clientId)
        .resultStream
        .mapToLong { it.toLong() }
        .toArray()

    private fun EntityManager.selectConfigPrefix(
        scope: Scope,
        prefix: String,
    ): LongArray = createQuery(
        "SELECT c.id FROM Config c " +
            "WHERE c.scope = :scope AND c.clientId = :clientId AND c.name LIKE :prefix " +
            "AND c.version = (" +
            "  SELECT max(c2.version) FROM Config c2 " +
            "  WHERE c2.scope = c.scope AND c2.clientId = c.clientId AND c2.name = c.name" +
            ")",
        java.lang.Long::class.java,
    )
        .setParameter("scope", scope.asString())
        .setParameter("clientId", clientId)
        .setParameter("prefix", "$prefix%")
        .resultStream
        .mapToLong { it.toLong() }
        .toArray()

    private fun EntityManager.selectConfigName(
        scopes: List<Scope>,
        name: QualifiedId,
    ): TypedQuery<ConfigEntity> = createQuery(
        "SELECT c FROM Config c " +
            "WHERE c.scope IN (:scopes) AND c.clientId = :clientId AND c.name = :name " +
            "AND c.version = (" +
            "  SELECT max(c2.version) FROM Config c2 " +
            "  WHERE c2.scope = c.scope AND c2.clientId = c.clientId AND c2.name = c.name" +
            ")",
        ConfigEntity::class.java,
    )
        .setParameter("scopes", scopes.map { it.asString() })
        .setParameter("clientId", clientId)
        .setParameter("name", name.asString())

    // New helper to select versions list for a single scope and id
    private fun EntityManager.selectConfigVersions(
        scope: Scope,
        id: QualifiedId,
    ): TypedQuery<ConfigEntity> = createQuery(
        "SELECT c FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId AND c.name = :name ORDER BY c.version DESC",
        ConfigEntity::class.java,
    )
        .setParameter("scope", scope.asString())
        .setParameter("clientId", clientId)
        .setParameter("name", id.asString())

    // New helper to select a specific version for a single scope and id
    private fun EntityManager.selectConfigVersion(
        scope: Scope,
        id: QualifiedId,
        version: Int,
    ): TypedQuery<ConfigEntity> = createQuery(
        "SELECT c FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId AND c.name = :name AND c.version = :version",
        ConfigEntity::class.java,
    )
        .setParameter("scope", scope.asString())
        .setParameter("clientId", clientId)
        .setParameter("name", id.asString())
        .setParameter("version", version)

    // Helper to compute the max version number
    private fun EntityManager.selectMaxVersion(
        scope: Scope,
        id: QualifiedId,
    ): Int? = createQuery(
        "SELECT MAX(c.version) FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId AND c.name = :name",
        java.lang.Integer::class.java,
    )
        .setParameter("scope", scope.asString())
        .setParameter("clientId", clientId)
        .setParameter("name", id.asString())
        .resultList
        .firstOrNull()
        ?.toInt()

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
        ConfigEntity::class.java,
    )
        .setParameter("scopes", scopes.map { it.asString() })
        .setParameter("clientId", clientId)

    private fun EntityManager.listConfigPrefix(
        scopes: List<Scope>,
        prefix: String,
    ): TypedQuery<ConfigEntity> = createQuery(
        "SELECT DISTINCT c.clientId, c.name FROM Config c WHERE c.scope IN (:scopes) AND c.clientId = :clientId AND c.name LIKE :prefix",
        ConfigEntity::class.java,
    )
        .setParameter("scopes", scopes.map { it.asString() })
        .setParameter("clientId", clientId)
        .setParameter("prefix", "$prefix%")

    companion object {
        private fun ConfigEntity.toResolvedVariable() = ResolvedVariable(
            SimpleScope(scope),
            QualifiedId(name),
            value?.toVariable() ?: NullLiteral(),
            createTimestamp,
            createdByUser,
            version,
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
