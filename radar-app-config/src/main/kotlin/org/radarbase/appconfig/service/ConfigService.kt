package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.GlobalConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.service.ConditionService.Companion.conditionScope
import org.radarbase.appconfig.service.MPProjectService.Companion.projectScope
import org.radarbase.jersey.auth.ProjectService
import java.util.stream.Stream
import javax.ws.rs.core.Context

class ConfigService(
        @Context private val resolver: ClientVariableResolver,
        @Context private val projectService: ProjectService,
        @Context private val conditionService: ConditionService,
        @Context private val clientService: ClientService
) {
    fun globalConfig(clientId: String): ClientConfig {
        return ClientConfig.fromStream(clientId,
                resolver[clientId].resolveAll(listOf(globalScope), null))
    }

    fun userConfig(clientId: String, projectId: String, userId: String): ClientConfig {
        clientService.ensureClient(clientId)
        val scopes = userScopes(clientId, projectId, userId)
        return ClientConfig.fromStream(clientId,
                resolver[clientId].resolveAll(scopes, null))
    }

    private fun userScopes(clientId: String, projectId: String, userId: String): List<Scope> {
        val conditions = conditionService.matchingConditions(clientId, projectId, userId)
                .map { conditionScope(it) }

        return (listOf(userScope(userId))
                + conditions
                + listOf(projectScope(projectId), globalScope))
    }

    fun putGlobalConfig(config: ClientConfig, clientId: String) {
        resolver[clientId].replace(globalScope, null, config.config.stream()
                .map { (innerId, variable, _) ->
                    QualifiedId(innerId) to
                            (variable?.toVariable() ?: NullLiteral())
                })
    }

    companion object {
        val globalScope: Scope = SimpleScope("global")
        fun userScope(userId: String): Scope = SimpleScope(QualifiedId("user", userId))
    }
}
