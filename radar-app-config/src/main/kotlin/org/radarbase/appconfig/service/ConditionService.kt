package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.Condition
import org.radarbase.appconfig.inject.ClientInterpreter
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.service.ConfigProjectServiceImpl.Companion.projectScope
import org.radarbase.appconfig.service.ConfigService.Companion.globalScope
import org.radarbase.appconfig.service.ConfigService.Companion.userScope
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.SimpleScope

class ConditionService(
    @Context private val resolver: ClientVariableResolver,
    @Context private val interpreter: ClientInterpreter,
) {
    suspend fun matchingConditions(clientId: String, projectId: String, userId: String?): List<Condition> {
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
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    fun list(projectId: String): List<Condition> {
        return listOf()
    }

    fun order(projectId: String, conditions: List<Condition>) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    fun update(projectId: String, condition: Condition): Condition {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    fun get(projectId: String, conditionId: Long): Condition {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    fun delete(projectId: String, conditionId: Long) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        fun conditionScope(condition: Condition): Scope = SimpleScope("condition.${condition.id}")
    }
}
