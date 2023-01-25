package org.radarbase.appconfig.service

import org.radarbase.lang.expression.NullLiteral
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.toVariable
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.service.ConfigService.Companion.userScope
import org.radarbase.appconfig.service.ConfigProjectServiceImpl.Companion.projectScope
import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.service.ConditionService.Companion.conditionScope
import org.radarbase.appconfig.service.ConfigService.Companion.globalScope

class UserService(
    @Context private val conditionService: ConditionService,
    @Context private val resolver: ClientVariableResolver,
) {
    fun putUserConfig(clientId: String, userId: String, clientConfig: ClientConfig) {
        resolver[clientId].replace(
            userScope(userId),
            null,
            clientConfig.config.asSequence()
                .map { (innerId, value, _) ->
                    Pair(QualifiedId(innerId), value?.toVariable() ?: NullLiteral())
                })
    }

    fun userConfig(
        clientId: String,
        projectId: String,
        userId: String,
    ): ClientConfig {
        val scopes = userScopes(clientId, projectId, userId)
        return ClientConfig.fromStream(
            clientId, scopes[0],
            resolver[clientId].resolveAll(scopes, null)
        )
    }

    private fun userScopes(
        clientId: String,
        projectId: String,
        userId: String,
    ): List<Scope> = buildList {
        add(userScope(userId))
        conditionService.matchingConditions(clientId, projectId, userId)
            .forEach { add(conditionScope(it)) }
        add(projectScope(projectId))
        add(globalScope)
    }
}
