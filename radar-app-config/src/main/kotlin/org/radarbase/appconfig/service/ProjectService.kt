package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.domain.GlobalConfig
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.appconfig.util.CachedSet
import org.radarbase.jersey.exception.HttpNotFoundException
import java.time.Duration
import javax.ws.rs.core.Context

class ProjectService(
        @Context private val mpClient: MPClient,
        @Context private val resolver: VariableResolver,
        @Context private val conditionService: ConditionService
): org.radarbase.jersey.auth.ProjectService {
    private val projects = CachedSet(Duration.ofMinutes(5), Duration.ofMinutes(1), mpClient::readProjects)

    override fun ensureProject(projectId: String) {
        if (projects.find { it.name == projectId } == null) {
            throw HttpNotFoundException("project_not_found", "Project $projectId not found.")
        }
    }

    fun projectConfig(projectId: String): GlobalConfig {
        val scopes = listOf(projectScope(projectId))
        return GlobalConfig.fromStream(resolver.resolveAll(scopes, null))
    }

    fun putConfig(projectId: String, globalConfig: GlobalConfig) {
        resolver.replace(projectScope(projectId), globalConfig.clients.values.stream()
                .flatMap { client ->
                    client.config.stream()
                            .map { (innerId, value, _) ->
                                Pair(QualifiedId("${client.clientId}.$innerId"), value?.toVariable() ?: NullLiteral())
                            }
                })
    }

    companion object {
        fun projectScope(projectId: String): Scope = SimpleScope(QualifiedId(listOf("project", projectId)))
    }
}