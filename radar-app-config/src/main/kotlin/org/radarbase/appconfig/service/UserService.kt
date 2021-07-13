package org.radarbase.appconfig.service

import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.service.ConfigService.Companion.userScope
import org.radarbase.appconfig.service.ConfigProjectServiceImpl.Companion.projectScope
import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.persistence.entity.ConfigStateEntity
import java.time.Instant

class UserService(
    @Context private val resolver: ClientVariableResolver,
) {
    fun putUserConfig(clientId: String, userId: String, clientConfig: ClientConfig) {
        resolver[clientId].replace(clientConfig.toVariableSet(userScope(userId)))
    }

    fun userConfig(clientId: String, projectId: String, userId: String): ClientConfig {
        val scope = userScope(userId)
        val defaultScopes = listOf(projectScope(projectId), ConfigService.globalScope)
        val clientResolver = resolver[clientId]
        return ClientConfig.fromStream(
            clientId = clientId,
            scope = scope,
            config = clientResolver.resolve(ConfigStateEntity.Type.CONFIG.name, scope),
            defaults = defaultScopes.mapNotNull { clientResolver.resolve(ConfigStateEntity.Type.CONFIG.name, it) },
        )
    }
}
