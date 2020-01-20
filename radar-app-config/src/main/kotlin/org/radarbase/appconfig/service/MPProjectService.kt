package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.Project
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.appconfig.service.ConfigService.Companion.userScope
import org.radarbase.appconfig.util.CachedSet
import java.time.Duration
import javax.ws.rs.core.Context

class MPProjectService(
        @Context private val mpClient: MPClient,
        @Context private val resolver: ClientVariableResolver,
        @Context private val conditionService: ConditionService,
        @Context private val clientService: ClientService
): ConfigProjectService {
    private val projects = CachedSet(Duration.ofMinutes(5), Duration.ofMinutes(1), mpClient::readProjects)

    override fun listProjects(): Set<Project> = projects.get()

    override fun projectConfig(clientId: String, projectId: String): ClientConfig {
        val scopes = listOf(projectScope(projectId))
        return ClientConfig.fromStream(clientId,
                resolver[clientId].resolveAll(scopes, null))
    }

    override fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig) {
        putConfig(clientId, projectScope(projectId), clientConfig)
    }

    override fun putUserConfig(clientId: String, userId: String, clientConfig: ClientConfig) {
        putConfig(clientId, userScope(userId), clientConfig)
    }


    override fun userConfig(clientId: String, projectId: String, userId: String): ClientConfig {
        clientService.ensureClient(clientId)
        val scopes = userScopes(clientId, projectId, userId)
        return ClientConfig.fromStream(clientId,
                resolver[clientId].resolveAll(scopes, null))
    }

    private fun userScopes(clientId: String, projectId: String, userId: String): List<Scope> {
        val conditions = conditionService.matchingConditions(clientId, projectId, userId)
                .map { ConditionService.conditionScope(it) }

        return (listOf(userScope(userId))
                + conditions
                + listOf(projectScope(projectId), ConfigService.globalScope))
    }

    private fun putConfig(clientId: String, scope: Scope, clientConfig: ClientConfig) {
        resolver[clientId].replace(
                scope,
                null,
                clientConfig.config.stream()
                        .map { (innerId, value, _) ->
                            Pair(QualifiedId(innerId), value?.toVariable() ?: NullLiteral())
                        })

    }

    companion object {
        fun projectScope(projectId: String): Scope = SimpleScope(QualifiedId(listOf("project", projectId)))
    }
}