package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.persistence.HibernateVariableResolver
import org.radarbase.appconfig.service.ConditionService.Companion.conditionScope
import org.radarbase.appconfig.service.ConfigProjectServiceImpl.Companion.projectScope
import org.radarbase.appconfig.service.ConfigService.Companion.globalScope
import org.radarbase.appconfig.service.ConfigService.Companion.userScope
import org.radarbase.jersey.exception.HttpNotFoundException
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

    suspend fun getUserConfig(clientId: String, projectId: String, userId: String,
    ): ClientConfig {
        val scopes = userScopes(clientId, projectId, userId)
        return ClientConfig.fromStream(
            clientId,
            scopes[0],
            resolver[clientId].resolveAll(scopes, null),
        )
    }

    suspend fun getUserConfigByName(
        clientId: String, projectId: String, userId: String, name: String,
    ): ClientConfig {
        val scopes = userScopes(clientId, projectId, userId)
        return ClientConfig.fromResolvedVariable(
            clientId,
            scopes[0],
            resolver[clientId].resolve(scopes, QualifiedId(name))
        )
    }

    suspend fun getUserConfigByNameAndVersion(
        clientId: String, projectId: String, userId: String, name: String, version: Int,
    ): ClientConfig {
        val scopes = userScopes(clientId, projectId, userId)
        return ClientConfig.fromVersionStream(
            clientId,
            scopes[0],
            resolver[clientId].resolveVersion(scopes, QualifiedId(name), version)
        )
    }

    suspend fun getUserConfigByNameAndAllVersions(clientId: String, projectId: String, userId: String, name: String,
    ): List<ClientConfig> {
        val scopes = userScopes(clientId, projectId, userId)
        val sequence = resolver[clientId].resolveVersions(scopes, QualifiedId(name))
        val config = ClientConfig.fromVersionStream(clientId, scopes[0], sequence)
        return listOf(config)
    }

    private suspend fun userScopes(clientId: String, projectId: String, userId: String,
    ): List<Scope> = buildList {
        add(userScope(userId))
        conditionService.matchingConditions(clientId, projectId, userId)
            .forEach { add(conditionScope(it)) }
        add(projectScope(projectId))
        add(globalScope)
    }
}
