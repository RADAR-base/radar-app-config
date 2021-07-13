package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.persistence.entity.ConfigStateEntity
import org.radarbase.appconfig.service.ConfigService.Companion.globalScope
import java.time.Instant

class ConfigProjectServiceImpl(
    @Context private val resolver: ClientVariableResolver,
) : ConfigProjectService {
    override fun projectConfig(clientId: String, projectId: String): ClientConfig {
        val scope = projectScope(projectId)
        val clientResolver = resolver[clientId]
        return ClientConfig.fromStream(
            clientId = clientId,
            scope = scope,
            config = clientResolver.resolve(ConfigStateEntity.Type.CONFIG.name, scope),
            defaults = listOfNotNull(clientResolver.resolve(ConfigStateEntity.Type.CONFIG.name, globalScope)),
        )
    }

    override fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig) {
        resolver[clientId].replace(clientConfig.toVariableSet(projectScope(projectId)))
    }

    companion object {
        fun projectScope(projectId: String): Scope = SimpleScope(QualifiedId(listOf("project", projectId)))
    }
}
