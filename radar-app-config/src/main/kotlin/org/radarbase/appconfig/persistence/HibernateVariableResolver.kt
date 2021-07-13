package org.radarbase.appconfig.persistence

import com.hazelcast.map.IMap
import jakarta.inject.Provider
import nl.thehyve.lang.expression.*
import org.hibernate.criterion.MatchMode
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.appconfig.persistence.entity.ConfigStateEntity
import org.radarbase.jersey.exception.HttpInternalServerException
import org.radarbase.jersey.hibernate.HibernateRepository
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.time.Instant
import javax.persistence.EntityManager
import javax.persistence.Query

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class HibernateVariableResolver(
    em: Provider<EntityManager>,
    private val clientId: String,
    private val latestConfigCache: IMap<String, Long>,
) : VariableResolver, HibernateRepository(em) {
    override fun replace(
        variableSet: VariableSet,
    ): Unit = transact {
        val type = variableSet.type.toConfigStateType()
        deactivateConfig(type, variableSet.scope).executeUpdate()

        val configState = ConfigStateEntity().apply {
            this.type = type
            clientId = this@HibernateVariableResolver.clientId
            scope = variableSet.scope.asString()
            lastModifiedAt = variableSet.lastModifiedAt ?: Instant.now()
            status = ConfigStateEntity.Status.ACTIVE
            values = emptyMap()
        }
        persist(configState)

        configState.values = variableSet.variables
            .asSequence()
            .filter { (id, _) -> !id.isEmpty() }
            .mapIndexed { i, (id, value) -> save(configState, id, value, i) }
            .associateBy { config -> config.name }
    }

    private fun String.toConfigStateType(): ConfigStateEntity.Type {
        return try {
            ConfigStateEntity.Type.valueOf(this)
        } catch (ex: IllegalArgumentException) {
            logger.error("Unknown configuration type {}", this)
            throw HttpInternalServerException("internal_server_error", "Internal server error")
        }
    }

    private fun EntityManager.save(
        configState: ConfigStateEntity,
        id: QualifiedId,
        variable: Variable,
        rank: Int,
    ): ConfigEntity {
        require(!id.isEmpty()) { "Cannot save variable without variable name" }

        val configEntity = ConfigEntity().apply {
            this.name = id.asString()
            this.value = variable.asOptString()
            this.state = configState
            this.rank = rank
        }
        persist(configEntity)
        return configEntity
    }

    override fun resolve(
        type: String,
        scopes: List<Scope>,
        id: QualifiedId,
    ): ResolvedVariable {
        val stateType = type.toConfigStateType()
        require(!id.isEmpty()) { "Cannot get variable without variable name" }
        return transact {
            selectConfigName(stateType, scopes, id)
                .arrayResultSequence
                .map { result: Array<*> ->
                    ResolvedVariable(
                        SimpleScope(result[0] as String),
                        id,
                        (result[1] as String?).toVariable(),
                    )
                }
                .minByOrNull { scopes.indexOf(it.scope) }
                ?: throw NoSuchFieldError("Unknown variable $id in scopes $scopes.")
        }
    }

    override fun resolve(
        type: String,
        scope: Scope,
    ): VariableSet? = transact {
        val stateType = type.toConfigStateType()
        val configStatus = selectConfig(stateType, scope)

        if (configStatus != null) {
            VariableSet(
                type = type,
                scope = SimpleScope(configStatus.scope),
                variables = configStatus.values.entries
                    .associate { (k, v) -> QualifiedId(k) to v.value.toVariable() },
                lastModifiedAt = configStatus.lastModifiedAt,
            )
        } else null
    }

    override fun list(
        type: String,
        scopes: List<Scope>,
        prefix: QualifiedId?,
    ): Sequence<QualifiedId> = transact {
        val stateType = type.toConfigStateType()
        listConfig(stateType, scopes, prefix)
            .arrayResultSequence
            .map { result: Array<Any> -> QualifiedId(clientId, result[0] as String) }
    }

    private fun EntityManager.deactivateConfig(
        type: ConfigStateEntity.Type,
        scope: Scope,
    ): Query = createQuery("""
        UPDATE ConfigState cs
        SET cs.status = 'INACTIVE'
        WHERE cs.type = :type
            AND cs.clientId = :clientId
            AND cs.scope = :scope
    """.trimIndent())
        .setParameter("type", type)
        .setParameter("clientId", clientId)
        .setParameter("scope", scope.asString())

    private fun EntityManager.selectConfig(
        type: ConfigStateEntity.Type,
        scope: Scope,
    ): ConfigStateEntity? {
        val scopeString = scope.asString()
        val cachedConfig = latestConfigCache[scopeString]
        return if (cachedConfig != null) {
            find(ConfigStateEntity::class.java, cachedConfig)
        } else {
            val query = createQuery(
                """
                    SELECT cs
                    FROM ConfigState cs
                    WHERE cs.type = :type
                        AND cs.clientId = :clientId
                        AND cs.scope = :scope
                        AND cs.status = 'ACTIVE'
                """.trimIndent(),
                ConfigStateEntity::class.java
            ).apply {
                setParameter("type", type)
                setParameter("clientId", clientId)
                setParameter("scope", scope.asString())
                maxResults = 1
            }
            query.resultList
                .firstOrNull()
                ?.also { latestConfigCache[scopeString] = it.id }
        }
    }

    private fun EntityManager.selectConfigName(
        type: ConfigStateEntity.Type,
        scopes: List<Scope>,
        name: QualifiedId,
    ): Query = createQuery("""
        SELECT cs.scope, c.value
        FROM Config c
            LEFT JOIN ConfigState cs ON c.state = cs
        WHERE cs.type = :type
            AND cs.clientId = :clientId
            AND cs.scope IN (:scopes)
            AND c.name = :name
    """.trimIndent())
        .setParameter("type", type)
        .setParameter("clientId", clientId)
        .setParameter("scopes", scopes.map { it.asString() })
        .setParameter("name", name.asString())

    private fun EntityManager.listConfig(
        type: ConfigStateEntity.Type,
        scopes: List<Scope>,
        prefix: QualifiedId?,
    ): Query = if (prefix == null || prefix.isEmpty()) {
        listConfig(type, scopes)
    } else {
        listConfigPrefix(type, scopes, prefix.asString())
    }

    private fun EntityManager.listConfig(
        type: ConfigStateEntity.Type,
        scopes: List<Scope>,
    ): Query = createQuery("""
        SELECT c.name, MIN(c.rank) as rank
        FROM Config c
          LEFT JOIN ConfigState cs ON c.state = cs
        WHERE cs.type = :type
          AND cs.clientId = :clientId
          AND cs.scope IN (:scopes)
        GROUP BY c.name
        ORDER BY rank
    """.trimIndent())
        .setParameter("type", type)
        .setParameter("clientId", clientId)
        .setParameter("scopes", scopes.map { it.asString() })

    private fun EntityManager.listConfigPrefix(
        type: ConfigStateEntity.Type,
        scopes: List<Scope>,
        prefix: String,
    ): Query = createQuery("""
        SELECT c.name, MIN(c.rank) as rank
        FROM Config c
            LEFT JOIN ConfigState cs ON c.state = cs
        WHERE cs.type = :type
            AND cs.clientId = :clientId
            AND cs.scope IN (:scopes)
            AND c.name LIKE :prefix
        GROUP BY c.name
        ORDER BY rank
    """.trimIndent())
        .setParameter("type", type)
        .setParameter("clientId", clientId)
        .setParameter("scopes", scopes.map { it.asString() })
        .setParameter("prefix", MatchMode.START.toMatchString(prefix))

    companion object {
        private val logger = LoggerFactory.getLogger(HibernateVariableResolver::class.java)

        @get:Suppress("UNCHECKED_CAST")
        val Query.arrayResultSequence: Sequence<Array<Any>>
            get() = (resultList as List<Array<Any>>).asSequence()
    }
}
