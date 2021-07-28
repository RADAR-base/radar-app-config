package org.radarbase.appconfig.persistence

import jakarta.inject.Provider
import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.persistence.entity.ConditionEntity
import org.radarbase.appconfig.persistence.entity.EntityStatus
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.hibernate.HibernateRepository
import java.time.Instant
import javax.persistence.EntityManager

class ConditionRepository(
    @Context em: Provider<EntityManager>,
) : HibernateRepository(em) {

    fun create(condition: ConditionEntity) = transact {
        getConditionOrNull(condition.projectId, condition.name)?.apply {
            status = EntityStatus.INACTIVE
        }?.also { merge(it) }
        persist(condition)
        condition
    }

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

    private fun EntityManager.getConditionOrNull(projectId: String, name: String): ConditionEntity? {
        return createQuery(
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
        }.resultList.firstOrNull()
    }

    private fun EntityManager.getCondition(projectId: String, name: String): ConditionEntity = getConditionOrNull(projectId, name)
        ?: throw HttpNotFoundException("condition_not_found", "Condition $name is not part of project $projectId")

    fun deactivate(projectId: String, conditionName: String): ConditionEntity = transact {
        getCondition(projectId, conditionName).apply {
            status = EntityStatus.INACTIVE
            deactivatedAt = Instant.now()
            lastModifiedAt = Instant.now()
        }.also { merge(it) }
    }

    fun update(conditionEntity: ConditionEntity) = transact {
        getCondition(conditionEntity.projectId, conditionEntity.name).apply {
            title = conditionEntity.title
            lastModifiedAt = Instant.now()
            expression = conditionEntity.expression
            rank = conditionEntity.rank
        }.also { merge(it) }
    }
}
