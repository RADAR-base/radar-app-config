package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import jakarta.ws.rs.core.Context

class MPProjectService(
    @Context private val resolver: ClientVariableResolver,
) : ConfigProjectService {
    override fun projectConfig(clientId: String, projectId: String): ClientConfig {
        val scope = projectScope(projectId)
        return ClientConfig.fromStream(
            clientId, scope,
            resolver[clientId].resolveAll(listOf(scope, ConfigService.globalScope), null)
        )
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

    companion object {
        fun projectScope(projectId: String): Scope = SimpleScope(QualifiedId(listOf("project", projectId)))
    }
}
