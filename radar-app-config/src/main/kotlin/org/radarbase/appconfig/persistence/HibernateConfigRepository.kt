package org.radarbase.appconfig.persistence

import com.hazelcast.core.HazelcastInstance
import jakarta.inject.Provider
import jakarta.ws.rs.core.Context
import java.time.Instant
import javax.persistence.EntityManager
import org.radarbase.appconfig.config.ConfigScope
import org.radarbase.appconfig.config.Scopes.toAppConfigScope
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.appconfig.persistence.entity.ConfigStateEntity
import org.radarbase.appconfig.persistence.entity.EntityStatus
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.lang.expression.*

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class HibernateConfigRepository(
    @Context em: Provider<EntityManager>,
    @Context private val hazelcastInstance: HazelcastInstance,
) : ConfigRepository, HibernateRepository(em) {
    override fun update(
        clientId: String,
        variableSet: VariableSet
    ): UpdateResult {
        val configScope = variableSet.scope.toAppConfigScope()
        require(configScope is ConfigScope) { "Cannot store scopes not intended for configuration" }

        val result = transact {
            val previousConfig = selectConfig(clientId, configScope)

            if (previousConfig != null) {
                val previousVariables = previousConfig.values.entries
                    .associate { (k, v) -> QualifiedId(k) to v.value.toVariable() }

                if (previousVariables == variableSet.variables) {
                    return@transact UpdateResult(requireNotNull(previousConfig.id), false)
                } else {
                    previousConfig.status = EntityStatus.INACTIVE
                    previousConfig.deactivatedAt = Instant.now()
                    merge(previousConfig)
                }
            }

            val configState = ConfigStateEntity(
                clientId = clientId,
                scope = configScope.asString(),
                lastModifiedAt = variableSet.lastModifiedAt ?: Instant.now(),
                status = EntityStatus.ACTIVE,
                values = emptyMap(),
            )
            persist(configState)
            configState.values = variableSet.variables
                .asSequence()
                .filter { (id, _) -> !id.isEmpty() }
                .mapIndexed { i, (id, value) -> save(configState, id, value, i.toFloat()) }
                .associateBy { config -> config.name }

            UpdateResult(requireNotNull(configState.id), true)
        }

        if (result.didUpdate) {
            hazelcastInstance.getMap<String, Long>(clientId)[configScope.asString()] = result.id
        }
        return result
    }

    override fun findActiveValue(
        clientId: String,
        scopes: List<Scope>,
        id: QualifiedId
    ): ResolvedVariable? {
        val configScopes = scopes
            .map { it.toAppConfigScope() }
            .filterIsInstance<ConfigScope>()

        if (configScopes.isEmpty()) return null

        return transact {
            val query = createQuery(
                """
                    SELECT cs.scope, c.value
                    FROM Config AS c LEFT JOIN ConfigState AS cs ON c.state = cs
                    WHERE cs.clientId = :clientId
                        AND cs.scope IN (:scopes)
                        AND cs.status = 'ACTIVE'
                        AND c.name = :name
                """.trimIndent()
            ).apply {
                setParameter("clientId", clientId)
                setParameter("scopes", configScopes)
                setParameter("name", id.asString())
            }

            @Suppress("UNCHECKED_CAST")
            (query.resultList as List<Array<String>>)
                .asSequence()
                .map { result -> ResolvedVariable(result[0].toAppConfigScope(), id, result[1].toVariable()) }
                .minByOrNull { scopes.indexOf(it.scope) }
        }
    }

    private fun EntityManager.save(
        configState: ConfigStateEntity,
        id: QualifiedId,
        variable: Variable,
        rank: Float,
    ): ConfigEntity {
        require(!id.isEmpty()) { "Cannot save variable without variable name" }

        return ConfigEntity(
            name = id.asString(),
            value = variable.asOptString(),
            state = configState,
            rank = rank,
        ).also { persist(it) }
    }

    override fun findActive(
        clientId: String,
        scope: Scope,
    ): VariableSet? {
        val configScope = scope.toAppConfigScope()
        require(configScope is ConfigScope) { "Cannot store scopes not intended for configuration" }

        return transact {
            val configStatus = selectConfig(clientId, configScope)

            if (configStatus != null) {
                VariableSet(
                    id = configStatus.id,
                    scope = configScope,
                    variables = configStatus.values.entries
                        .associate { (k, v) -> QualifiedId(k) to v.value.toVariable() },
                    lastModifiedAt = configStatus.lastModifiedAt,
                )
            } else null
        }
    }

    override fun get(id: Long): ClientVariableSet? = transact {
        val configStatus = find(ConfigStateEntity::class.java, id)
        if (configStatus != null) {
            ClientVariableSet(
                configStatus.clientId,
                VariableSet(
                    id = configStatus.id,
                    scope = configStatus.scope.toAppConfigScope(),
                    variables = configStatus.values.entries
                        .associate { (k, v) -> QualifiedId(k) to v.value.toVariable() },
                    lastModifiedAt = configStatus.lastModifiedAt,
                ),
            )
        } else null
    }

    private fun EntityManager.selectConfig(
        clientId: String,
        scope: ConfigScope,
    ): ConfigStateEntity? {
        val scopeString = scope.asString()
        val latestConfigCache = hazelcastInstance.getMap<String, Long>(clientId)
        val cachedConfig = latestConfigCache[scopeString]
        return if (cachedConfig != null) {
            find(ConfigStateEntity::class.java, cachedConfig)
        } else {
            val query = createQuery(
                """
                    SELECT cs
                    FROM ConfigState cs
                    WHERE cs.clientId = :clientId
                        AND cs.scope = :scope
                        AND cs.status = 'ACTIVE'
                """.trimIndent(),
                ConfigStateEntity::class.java
            ).apply {
                setParameter("clientId", clientId)
                setParameter("scope", scope.asString())
                maxResults = 1
            }
            query.resultList
                .firstOrNull()
                ?.also { latestConfigCache[scopeString] = it.id }
        }
    }
}
