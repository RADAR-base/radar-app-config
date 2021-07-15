package org.radarbase.appconfig.persistence

import com.hazelcast.core.HazelcastInstance
import jakarta.inject.Provider
import jakarta.ws.rs.core.Context
import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.appconfig.persistence.entity.ConfigStateEntity
import org.radarbase.jersey.hibernate.HibernateRepository
import java.time.Instant
import javax.persistence.EntityManager

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class HibernateConfigRepository(
    @Context em: Provider<EntityManager>,
    @Context private val hazelcastInstance: HazelcastInstance,
) : ConfigRepository, HibernateRepository(em) {
    override fun update(
        clientId: String,
        variableSet: VariableSet
    ): UpdateResult {
        val result = transact {
            val previousConfig = selectConfig(clientId, variableSet.scope)

            if (previousConfig != null) {
                val previousVariables = previousConfig.values.entries
                    .associate { (k, v) -> QualifiedId(k) to v.value.toVariable() }

                if (previousVariables == variableSet.variables) {
                    return@transact UpdateResult(requireNotNull(previousConfig.id), false)
                } else {
                    previousConfig.status = ConfigStateEntity.Status.INACTIVE
                    merge(previousConfig)
                }
            }

            val configState = ConfigStateEntity().apply {
                this.clientId = clientId
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

            UpdateResult(requireNotNull(configState.id), true)
        }

        if (result.didUpdate) {
            hazelcastInstance.getMap<String, Long>(clientId)[variableSet.scope.asString()] = result.id
        }
        return result
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

    override fun findActive(
        clientId: String,
        scope: Scope,
    ): VariableSet? = transact {
        val configStatus = selectConfig(clientId, scope)

        if (configStatus != null) {
            VariableSet(
                id = configStatus.id,
                scope = SimpleScope(configStatus.scope),
                variables = configStatus.values.entries
                    .associate { (k, v) -> QualifiedId(k) to v.value.toVariable() },
                lastModifiedAt = configStatus.lastModifiedAt,
            )
        } else null
    }

    override fun get(id: Long): ClientVariableSet? = transact {
        val configStatus = find(ConfigStateEntity::class.java, id)
        if (configStatus != null) {
            ClientVariableSet(
                configStatus.clientId,
                VariableSet(
                    id = configStatus.id,
                    scope = SimpleScope(configStatus.scope),
                    variables = configStatus.values.entries
                        .associate { (k, v) -> QualifiedId(k) to v.value.toVariable() },
                    lastModifiedAt = configStatus.lastModifiedAt,
                ),
            )
        } else null
    }

    private fun EntityManager.selectConfig(
        clientId: String,
        scope: Scope,
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
//
//    private fun EntityManager.selectConfigName(
//        clientId: String,
//        scopes: List<Scope>,
//        name: QualifiedId,
//    ): Query = createQuery("""
//        SELECT cs.scope, c.value
//        FROM Config c
//            LEFT JOIN ConfigState cs ON c.state = cs
//        WHERE cs.clientId = :clientId
//            AND cs.scope IN (:scopes)
//            AND c.name = :name
//    """.trimIndent())
//        .setParameter("clientId", clientId)
//        .setParameter("scopes", scopes.map { it.asString() })
//        .setParameter("name", name.asString())
//
//    companion object {
//        @get:Suppress("UNCHECKED_CAST")
//        val Query.arrayResultSequence: Sequence<Array<Any>>
//            get() = (resultList as List<Array<Any>>).asSequence()
//    }
}
