package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.NullLiteral
import nl.thehyve.lang.expression.QualifiedId
import nl.thehyve.lang.expression.Scope
import nl.thehyve.lang.expression.toVariable
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.service.ConfigService.Companion.userScope
import org.radarbase.appconfig.service.ConfigProjectServiceImpl.Companion.projectScope
import jakarta.ws.rs.core.Context

class UserService(
    @Context private val conditionService: ConditionService,
    @Context private val resolver: ClientVariableResolver,
) {
    fun putUserConfig(clientId: String, userId: String, clientConfig: ClientConfig) {
        resolver[clientId].replace(
            userScope(userId),
            null,
            clientConfig.config.stream()
                .map { (innerId, value, _) ->
                    Pair(QualifiedId(innerId), value?.toVariable() ?: NullLiteral())
                })
    }

    fun userConfig(clientId: String, projectId: String, userId: String): ClientConfig {
        val scopes = userScopes(clientId, projectId, userId)
        return ClientConfig.fromStream(
            clientId, scopes[0],
            resolver[clientId].resolveAll(scopes, null)
        )
    }

    private fun userScopes(clientId: String, projectId: String, userId: String): List<Scope> {
        val conditions = conditionService.matchingConditions(clientId, projectId, userId)
            .map { ConditionService.conditionScope(it) }

        return (listOf(userScope(userId))
            + conditions
            + listOf(projectScope(projectId), ConfigService.globalScope))
    }
}
