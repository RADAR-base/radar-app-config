package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.persistence.HibernateVariableResolver
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.lang.expression.NullLiteral
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.SimpleScope
import org.radarbase.lang.expression.toVariable

class ConfigProjectServiceImpl(
    @Context private val resolver: ClientVariableResolver,
) : ConfigProjectService {
    override suspend fun projectConfig(clientId: String, projectId: String): ClientConfig? {
        val scope = projectScope(projectId)
        val vr = resolver[clientId]
        return if (vr is HibernateVariableResolver) {
            val projectMostRecent = vr.mostRecentConfigs(scope)
            val globalMostRecent = vr.mostRecentConfigs(ConfigService.globalScope)

            ClientConfig(
                clientId = clientId,
                scope = scope.asString(),
                config = projectMostRecent.flatMap { it.config },
                defaults = globalMostRecent.flatMap { it.config },
            )
        } else {
            null
        }
    }

    override suspend fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig) {
        resolver[clientId].replace(
            projectScope(projectId),
            null,
            clientConfig.config.asSequence()
                .map { (innerId, value, _) ->
                    Pair(QualifiedId(innerId), value?.toVariable() ?: NullLiteral())
                },
        )
    }

    override suspend fun projectConfigName(projectId: String, clientId: String, name: String): ClientConfig? {
        val vr = resolver[clientId]
        return if (vr is HibernateVariableResolver) {
            // First try project-scoped configs. If none found, fall back to global defaults
            vr.versions(projectScope(projectId), QualifiedId(name)).firstOrNull()
                ?: vr.versions(ConfigService.globalScope, QualifiedId(name)).firstOrNull()
        } else {
            null
        }
    }

    override suspend fun projectConfigNameVersions(projectId: String, clientId: String, name: String): List<ClientConfig> {
        val vr = resolver[clientId]
        return if (vr is HibernateVariableResolver) {
            vr.versions(projectScope(projectId), QualifiedId(name))
        } else {
            emptyList()
        }
    }

    override suspend fun projectConfigNameVersion(projectId: String, clientId: String, name: String, version: Int): ClientConfig {
        val vr = resolver[clientId]
        val versions = if (vr is HibernateVariableResolver) {
            vr.versions(projectScope(projectId), QualifiedId(name))
        } else emptyList()

        return versions.firstOrNull { it.config.firstOrNull()?.version == version }
            ?: throw HttpNotFoundException(
                "config_version_not_found",
                "No config found for name '$name' with version $version in scope '${projectScope(projectId).asString()}' for client $clientId.",
            )
    }

    companion object {
        fun projectScope(projectId: String): Scope = SimpleScope(QualifiedId(listOf("project", projectId)))
    }
}
