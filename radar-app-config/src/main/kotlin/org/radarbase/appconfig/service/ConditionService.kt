package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import java.lang.UnsupportedOperationException
import java.time.Instant
import org.radarbase.appconfig.condition.ClientInterpreter
import org.radarbase.appconfig.config.ConditionScope
import org.radarbase.appconfig.config.ProjectScope
import org.radarbase.appconfig.config.Scopes.GLOBAL_CONFIG_SCOPE
import org.radarbase.appconfig.config.Scopes.config
import org.radarbase.appconfig.config.Scopes.dynamic
import org.radarbase.appconfig.config.UserScope
import org.radarbase.appconfig.domain.Condition
import org.radarbase.appconfig.domain.Condition.Companion.toCondition
import org.radarbase.appconfig.persistence.ConditionRepository
import org.radarbase.appconfig.persistence.entity.ConditionEntity
import org.radarbase.appconfig.persistence.entity.EntityStatus
import org.radarbase.jersey.exception.HttpInternalServerException
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.lang.expression.ExpressionParser
import org.radarbase.lang.expression.InterpreterException
import org.slf4j.LoggerFactory

open class ConditionService(
    @Context private val interpreter: ClientInterpreter,
    @Context private val conditionRepository: ConditionRepository,
    @Context private val expressionParser: ExpressionParser,
) {
    open fun matchingScopes(clientId: String, projectId: String, userId: String): List<ConditionScope> {
        val allConditions = conditionRepository.list(projectId)
            .filter { it.expression != null }
            .map { it.toCondition(expressionParser) }

        val projectScope = ProjectScope(projectId)
        val conditionScopes = listOf(
            UserScope(userId).config,
            UserScope(userId, projectScope).dynamic,
            projectScope.config,
            projectScope.dynamic,
            GLOBAL_CONFIG_SCOPE,
        )

        return allConditions
            .filter { try {
                interpreter[clientId].interpret(conditionScopes, it.expression!!).asBoolean()
            } catch (ex: Exception) {
                logger.warn("Failed to evaluate condition ${it.name} in project $projectId: {}", ex.toString())
                false
            }}
            .map { ConditionScope(it.name, projectScope) }
    }

    fun evaluate(clientId: String, projectId: String, conditionName: String, userId: String): Pair<Condition, Any?> {
        val condition = (conditionRepository.get(projectId, conditionName)
            ?: throw HttpNotFoundException("condition_not_found", "No condition $conditionName in project $projectId"))
            .toCondition(expressionParser)

        val expression = condition.expression
            ?: throw HttpNotFoundException("condition_expression_not_found", "No condition expression $conditionName in project $projectId")

        val projectScope = ProjectScope(projectId)
        val conditionScopes = listOf(
            UserScope(userId).config,
            UserScope(userId, projectScope).dynamic,
            projectScope.config,
            projectScope.dynamic,
            GLOBAL_CONFIG_SCOPE,
        )

        try {
            return Pair(condition, interpreter[clientId].interpret(conditionScopes, expression).asRegularObject())
        } catch (ex: InterpreterException) {
            throw HttpInternalServerException("evaluation_error", "Cannot evaluate expression '${ex.expression}':  ${ex.message}.")
        }
    }

    fun create(projectId: String, condition: Condition): Condition {
        val conditionEntity = conditionRepository.create(
            ConditionEntity(
                projectId = projectId,
                name = condition.name,
                title = condition.title,
                rank = condition.rank,
                expression = condition.expression?.toString(),
                createdAt = Instant.now(),
                lastModifiedAt = Instant.now(),
                status = EntityStatus.ACTIVE,
            )
        )
        return condition.copy(
            id = conditionEntity.id,
            lastModifiedAt = conditionEntity.lastModifiedAt,
        )
    }

    fun list(projectId: String): List<Condition> {
        return conditionRepository.list(projectId)
            .map { it.toCondition(expressionParser) }
    }

    fun update(projectId: String, condition: Condition): Condition {
        return conditionRepository
            .update(
                ConditionEntity(
                    projectId = projectId,
                    name = condition.name,
                    title = condition.title,
                    rank = condition.rank,
                    expression = condition.expression?.toString(),
                    status = EntityStatus.ACTIVE,
                    lastModifiedAt = Instant.now(),
                ),
            )
            .toCondition(expressionParser)
    }

    fun get(projectId: String, conditionName: String): Condition {
        val conditionEntity = conditionRepository.get(projectId, conditionName)
            ?: throw HttpNotFoundException("condition_not_found", "Cannot find matching condition $conditionName in project $projectId")

        return conditionEntity.toCondition(expressionParser)
    }

    fun deactivate(projectId: String, conditionName: String) {
        conditionRepository.deactivate(projectId, conditionName)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConditionService::class.java)
    }
}
