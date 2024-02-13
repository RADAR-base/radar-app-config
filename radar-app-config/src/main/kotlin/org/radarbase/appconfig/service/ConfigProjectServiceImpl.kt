package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.lang.expression.*

class ConfigProjectServiceImpl(
    @Context private val resolver: ClientVariableResolver,
) : ConfigProjectService {
    override suspend fun projectConfig(clientId: String, projectId: String): ClientConfig {
        val scope = projectScope(projectId)
        return ClientConfig.fromStream(
            clientId, scope,
            resolver[clientId].resolveAll(listOf(scope, ConfigService.globalScope), null)
        )
    }

    override suspend fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig) {
        resolver[clientId].replace(
            projectScope(projectId),
            null,
            clientConfig.config.asSequence()
                .map { (innerId, value, _) ->
                    Pair(QualifiedId(innerId), value?.toVariable() ?: NullLiteral())
                })
    }

    companion object {
        fun projectScope(projectId: String): Scope = SimpleScope(QualifiedId(listOf("project", projectId)))
    }
}
