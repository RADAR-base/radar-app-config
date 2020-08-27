package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.Project
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.appconfig.util.CachedSet
import org.radarbase.jersey.auth.Auth
import org.radarcns.auth.authorization.Permission
import java.time.Duration
import javax.ws.rs.core.Context

class MPProjectService(
        @Context private val auth: Auth,
        @Context private val mpClient: MPClient,
        @Context private val resolver: ClientVariableResolver
): ConfigProjectService {
    private val projects = CachedSet(Duration.ofMinutes(5), Duration.ofMinutes(1), mpClient::readProjects)

    override operator fun contains(name: String) = projects.get().any { it.name == name }

    override fun listProjects(): Set<Project> = projects.get()
            .filterTo(LinkedHashSet()) { auth.token.hasPermissionOnProject(Permission.PROJECT_READ, it.name) }

    override fun projectConfig(clientId: String, projectId: String): ClientConfig {
        val scope = projectScope(projectId)
        return ClientConfig.fromStream(clientId, scope,
                resolver[clientId].resolveAll(listOf(scope, ConfigService.globalScope), null))
    }

    override fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig) {
        resolver[clientId].replace(
                projectScope(projectId),
                null,
                clientConfig.config.stream()
                        .map { (innerId, value, _) ->
                            Pair(QualifiedId(innerId), value?.toVariable() ?: NullLiteral())
                        })
    }

    override fun find(projectId: String): Project? = projects.find { it.name == projectId }
            ?.takeIf { auth.token.hasPermissionOnProject(Permission.PROJECT_READ, it.name) }

    companion object {
        fun projectScope(projectId: String): Scope = SimpleScope(QualifiedId(listOf("project", projectId)))
    }
}
