package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.GlobalConfig
import org.radarbase.appconfig.service.ConditionService.Companion.conditionScope
import org.radarbase.appconfig.service.MPProjectService.Companion.projectScope
import org.radarbase.jersey.auth.ProjectService
import javax.ws.rs.core.Context

class ConfigService(
        @Context private val resolver: VariableResolver,
        @Context private val projectService: ProjectService,
        @Context private val conditionService: ConditionService,
        @Context private val clientService: ClientService
) {
    fun clientConfig(clientId: String, projectId: String, userId: String): ClientConfig {
        clientService.ensureClient(clientId)
        val scopes = userScopes(projectId, userId)
        return ClientConfig.fromStream(clientId, resolver.resolveAll(scopes, QualifiedId(listOf(clientId))))
    }

    fun globalConfig(projectId: String, userId: String): GlobalConfig {
        projectService.ensureProject(projectId)
        val scopes = userScopes(projectId, userId)
        return GlobalConfig.fromStream(resolver.resolveAll(scopes, null)
                .filter { (_, id, _) -> id.names.firstOrNull()
                        ?.let { clientService.contains(it) }
                        ?: false })
    }

    private fun userScopes(projectId: String, userId: String): List<Scope> {
        val conditions = conditionService.matchingConditions(projectId, userId)
                .map { conditionScope(it) }

        return (listOf(userScope(userId))
                + conditions
                + listOf(projectScope(projectId), globalScope))
    }

    fun putConfig(config: GlobalConfig) {
        resolver.replace(globalScope, config.clients.values.stream()
                .flatMap { clientConfig ->
                    clientConfig.config.stream()
                            .map { (innerId, variable, _) ->
                                QualifiedId("${clientConfig.clientId}.$innerId") to
                                        (variable?.toVariable() ?: NullLiteral())
                            }
                })
    }

    companion object {
        val globalScope: Scope = SimpleScope("global")
        fun userScope(userId: String): Scope = SimpleScope(QualifiedId("user", userId))
    }
}