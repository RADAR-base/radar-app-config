package org.radarbase.appconfig.persistence

import jakarta.inject.Provider
import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.persistence.entity.ConditionEntity
import org.radarbase.jersey.hibernate.HibernateRepository
import javax.persistence.EntityManager

class ConditionRepository(
    @Context em: Provider<EntityManager>,
): HibernateRepository(em) {

    fun create(condition: ConditionEntity) = transact {
        val existingCondition = getCondition(condition.projectId, condition.name)
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

    operator fun get(conditionId: Long): ConditionEntity = transact {
        find(ConditionEntity::class.java, conditionId)
    }

    private fun EntityManager.getCondition(projectId: String, name: String): ConditionEntity? {
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
}
