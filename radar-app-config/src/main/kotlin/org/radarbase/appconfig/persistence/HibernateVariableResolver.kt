package org.radarbase.appconfig.persistence

import com.hazelcast.map.IMap
import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import jakarta.persistence.TypedQuery
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.SingleVariable
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
        val mostRecent = findMostRecentConfig(scope, id)
        val nextVersion = ((mostRecent?.version) ?: 0) + 1

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

    /**
     * List all versions for the given scope and variable name for this client.
     * Results are ordered with newest version first.
     */
    suspend fun versions(scope: Scope, name: QualifiedId): List<ClientConfig> = transact {
        createQuery(
            "SELECT c FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId AND c.name = :name ORDER BY c.version DESC",
            ConfigEntity::class.java,
        )
            .setParameter("scope", scope.asString())
            .setParameter("clientId", clientId)
            .setParameter("name", name.asString())
            .resultList
            .map { e ->
                ClientConfig(
                    clientId = clientId,
                    scope = e.scope,
                    config = listOf(
                        SingleVariable(
                            name = e.name,
                            value = e.value,
                            scope = e.scope,
                            clientId = e.clientId,
                            version = e.version,
                            createdByUser = e.createdByUser,
                            createTimestamp = (e.createTimestamp ?: java.time.Instant.EPOCH).toEpochMilli(),
                        ),
                    ),
                    defaults = null,
                )
            }
    }

    /**
     * Return the most recent configuration for each variable name for the given scope and client.
     */
    suspend fun mostRecentConfigs(scope: Scope): List<ClientConfig> = transact {
        selectConfig(scope)
            .asSequence()
            .map { id -> find(ConfigEntity::class.java, id) }
            .map { e ->
                ClientConfig(
                    clientId = clientId,
                    scope = e.scope,
                    config = listOf(
                        SingleVariable(
                            name = e.name,
                            value = e.value,
                            scope = e.scope,
                            clientId = e.clientId,
                            version = e.version,
                            createdByUser = e.createdByUser,
                            createTimestamp = (e.createTimestamp ?: java.time.Instant.EPOCH).toEpochMilli(),
                        ),
                    ),
                    defaults = null,
                )
            }
            .toList()
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
            "AND c.createTimestamp = (" +
            "  SELECT max(c2.createTimestamp) FROM Config c2 " +
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
            "AND c.createTimestamp = (" +
            "  SELECT max(c2.createTimestamp) FROM Config c2 " +
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
            "AND c.createTimestamp = (" +
            "  SELECT max(c2.createTimestamp) FROM Config c2 " +
            "  WHERE c2.scope = c.scope AND c2.clientId = c.clientId AND c2.name = c.name" +
            ")",
        ConfigEntity::class.java,
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

    /**
     * Find the most recent configuration entity for the given client, scope and name,
     * ordered by createTimestamp descending. Returns null if no entity exists.
     */
    private fun EntityManager.findMostRecentConfig(
        scope: Scope,
        name: QualifiedId,
    ): ConfigEntity? = createQuery(
        "SELECT c FROM Config c WHERE c.scope = :scope AND c.clientId = :clientId AND c.name = :name ORDER BY c.createTimestamp DESC",
        ConfigEntity::class.java,
    )
        .setParameter("scope", scope.asString())
        .setParameter("clientId", clientId)
        .setParameter("name", name.asString())
        .setMaxResults(1)
        .resultList
        .firstOrNull()

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
