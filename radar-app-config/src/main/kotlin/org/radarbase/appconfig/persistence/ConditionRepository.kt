package org.radarbase.appconfig.persistence

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import java.time.Instant
import org.radarbase.appconfig.config.ConditionScope.Companion.conditionScopeString
import org.radarbase.appconfig.config.Scopes.CONDITION_TOKEN
import org.radarbase.appconfig.persistence.entity.ConditionEntity
import org.radarbase.appconfig.persistence.entity.EntityStatus
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.hibernate.HibernateRepository

class ConditionRepository(
    @Context em: Provider<EntityManager>,
    @Context private val hazelcastInstance: HazelcastInstance,
) : HibernateRepository(em) {
    private val conditionCache: IMap<String, Long> = hazelcastInstance.getMap(CONDITION_TOKEN)

    fun create(condition: ConditionEntity) = transact {
        getConditionOrNull(condition.projectId, condition.name)?.apply {
            status = EntityStatus.INACTIVE
        }?.also { merge(it) }
        persist(condition)
        condition
    }.cached()

    fun list(projectId: String): List<ConditionEntity> = transact {
        createQuery(
            """
                SELECT ce
                FROM Condition ce
                WHERE ce.projectId = :projectId
                    AND ce.status = 'ACTIVE'
                ORDER BY ce.rank, ce.name
            """.trimIndent(),
            ConditionEntity::class.java,
        ).apply {
            setParameter("projectId", projectId)
        }.resultList
    }

    fun get(projectId: String, conditionName: String): ConditionEntity? = transact {
        getConditionOrNull(projectId, conditionName)
    }

    fun deactivate(projectId: String, conditionName: String): ConditionEntity = transact {
        getCondition(projectId, conditionName).apply {
            status = EntityStatus.INACTIVE
            deactivatedAt = Instant.now()
            lastModifiedAt = Instant.now()
        }.also { merge(it) }
    }.uncached()

    fun update(condition: ConditionEntity) = transact {
        getCondition(condition.projectId, condition.name).apply {
            title = condition.title
            lastModifiedAt = Instant.now()
            expression = condition.expression
            rank = condition.rank
        }.also { merge(it) }
    }.cached()

    private fun EntityManager.getConditionOrNull(projectId: String, name: String): ConditionEntity? {
        val cache = hazelcastInstance.getMap<String, Long>(CONDITION_TOKEN)
        val cachedId = cache[conditionScopeString(name, projectId)]
        return if (cachedId != null) {
            find(ConditionEntity::class.java, cachedId)
        } else {
            val query = createQuery(
                """
                    SELECT ce
                    FROM Condition ce
                    WHERE ce.projectId = :projectId
                        AND ce.status = 'ACTIVE'
                        AND ce.name = :name
                """.trimIndent(),
                ConditionEntity::class.java,
            ).apply {
                setParameter("projectId", projectId)
                setParameter("name", name)
            }
            query.resultList
                .firstOrNull()
        }
    }

    private fun EntityManager.getCondition(projectId: String, name: String): ConditionEntity = getConditionOrNull(projectId, name)
        ?: throw HttpNotFoundException("condition_not_found", "Condition $name is not part of project $projectId")

    private fun ConditionEntity.uncached(): ConditionEntity = apply {
        conditionCache -= conditionScopeString(name, projectId)
    }

    private fun ConditionEntity.cached(): ConditionEntity = apply {
        conditionCache[conditionScopeString(name, projectId)] = id
    }
}
