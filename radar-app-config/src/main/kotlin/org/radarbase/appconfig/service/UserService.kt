package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.NullLiteral
import nl.thehyve.lang.expression.QualifiedId
import nl.thehyve.lang.expression.Scope
import nl.thehyve.lang.expression.toVariable
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.User
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.appconfig.service.ConfigService.Companion.userScope
import org.radarbase.appconfig.service.MPProjectService.Companion.projectScope
import org.radarbase.appconfig.util.CachedSet
import org.radarbase.jersey.exception.HttpNotFoundException
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import javax.ws.rs.core.Context

class UserService(
        @Context private val mpClient: MPClient,
        @Context private val conditionService: ConditionService,
        @Context private val resolver: ClientVariableResolver
) {
    private val users = ConcurrentHashMap<String, CachedSet<User>>()

    private operator fun get(projectId: String) = users.computeIfAbsent(projectId) {
        CachedSet(Duration.ofMinutes(5), Duration.ofMinutes(1)) {
            mpClient.readUsers(projectId)
        }
    }

    fun ensureUser(projectId: String, userId: String) {
        if (this[projectId].find { it.id == userId } == null) {
            throw HttpNotFoundException("user_missing", "User $userId not found in project $projectId")
        }
    }

    fun list(projectId: String) = this[projectId].get()

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
        return ClientConfig.fromStream(clientId, scopes[0],
                resolver[clientId].resolveAll(scopes, null))
    }

    private fun userScopes(clientId: String, projectId: String, userId: String): List<Scope> {
        val conditions = conditionService.matchingConditions(clientId, projectId, userId)
                .map { ConditionService.conditionScope(it) }

        return (listOf(userScope(userId))
                + conditions
                + listOf(projectScope(projectId), ConfigService.globalScope))
    }

    fun find(projectId: String, userId: String): User? = this[projectId].find { it.id == userId }
}