package org.radarbase.appconfig.service

import org.radarbase.appconfig.domain.Condition
import org.radarbase.appconfig.inject.ClientInterpreter
import org.radarbase.appconfig.service.ConfigService.Companion.globalScope
import org.radarbase.appconfig.service.ConfigService.Companion.projectScope
import org.radarbase.appconfig.service.ConfigService.Companion.userScope
import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.persistence.ConfigRepository
import org.radarbase.lang.expression.*


class ConditionService(
    @Context private val interpreter: ClientInterpreter,
    @Context private val configRepository: ConfigRepository,
) {
    fun matchingConditions(clientId: String, projectId: String, userId: String?): List<Condition> {
        val allConditions = listOf<Condition>()

        val conditionScopes = mutableListOf<Scope>()

        userId?.let {
            conditionScopes += userScope(it)
            // TODO: Add user event scope
        }
        conditionScopes += projectScope(projectId)
        conditionScopes += globalScope

        return allConditions
            .filter { interpreter[clientId].interpret(conditionScopes, it.expression).asBoolean() }
    }

    fun create(projectId: String, condition: Condition): Condition {
        configRepositorgity.update("", VariableSet(
            scope = projectScope(projectId).asConditionScope(),
            variables = mapOf(
                QualifiedId("expression") to NullLiteral()
            )
        )
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun list(projectId: String): List<Condition> {
        return listOf()
    }

    fun order(projectId: String, conditions: List<Condition>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun update(projectId: String, condition: Condition): Condition {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun get(projectId: String, conditionId: Long): Condition {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun delete(projectId: String, conditionId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        fun Scope.asConditionScope() = prefixWith("condition")
        fun conditionScope(condition: Condition): Scope = SimpleScope("condition.${condition.id}")
    }
}
