package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.service.ConditionService.Companion.conditionScope
import org.radarbase.appconfig.service.ConfigProjectServiceImpl.Companion.projectScope
import org.radarbase.appconfig.service.ConfigService.Companion.globalScope
import org.radarbase.appconfig.service.ConfigService.Companion.userScope
import org.radarbase.lang.expression.NullLiteral
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.toVariable

class UserService(
    @Context private val conditionService: ConditionService,
    @Context private val resolver: ClientVariableResolver,
) {
    suspend fun putUserConfig(clientId: String, userId: String, clientConfig: ClientConfig) {
        resolver[clientId].replace(
            userScope(userId),
            null,
            clientConfig.config.asSequence()
                .map { (innerId, value, _) ->
                    Pair(QualifiedId(innerId), value?.toVariable() ?: NullLiteral())
                },
        )
    }

    suspend fun userConfig(
        clientId: String,
        projectId: String,
        userId: String,
    ): ClientConfig {
        val scopes = userScopes(clientId, projectId, userId)
        return ClientConfig.fromStream(
            clientId,
            scopes[0],
            resolver[clientId].resolveAll(scopes, null),
        )
    }

    private suspend fun userScopes(
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
