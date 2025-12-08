package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.service.ConfigService.Companion.globalScope
import org.radarbase.lang.expression.NullLiteral
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.SimpleScope
import org.radarbase.lang.expression.toVariable

class ConfigProjectServiceImpl(
    @Context private val resolver: ClientVariableResolver,
) : ConfigProjectService {
    override suspend fun getProjectConfig(clientId: String, projectId: String): ClientConfig {
        val scope = projectScope(projectId)
        return ClientConfig.fromStream(
            clientId,
            scope,
            resolver[clientId].resolveAll(listOf(scope, globalScope), null),
        )
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

    override suspend fun getProjectConfigByName(projectId: String, clientId: String, name: String): ClientConfig {
        val scope = projectScope(projectId)
        return ClientConfig.fromResolvedVariable(
            clientId,
            scope,
            resolver[clientId].resolve(listOf(scope, globalScope), QualifiedId(name)),
        )
    }

    override suspend fun getProjectConfigByNameAndVersion(projectId: String, clientId: String, name: String, version: Int): ClientConfig {
        val scope = projectScope(projectId)
        return ClientConfig.fromVersionStream(
            clientId,
            scope,
            resolver[clientId].resolveVersion(listOf(scope), QualifiedId(name), version),
        )
    }

    override suspend fun getProjectConfigByNameAndAllVersions(projectId: String, clientId: String, name: String): ClientConfig {
        val scope = projectScope(projectId)
        val sequence = resolver[clientId].resolveVersions(listOf(scope), QualifiedId(name))
        val config = ClientConfig.fromVersionStream(clientId, scope, sequence)
        return config
    }

    companion object {
        fun projectScope(projectId: String): Scope = SimpleScope(QualifiedId(listOf("project", projectId)))
    }
}
