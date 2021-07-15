package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.domain.ClientConfig
import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.persistence.ConfigRepository
import org.radarbase.appconfig.service.ConfigService.Companion.configScope

class ConfigService(
    @Context private val configRepository: ConfigRepository,
) {
    fun globalConfig(clientId: String): ClientConfig {
        return ClientConfig.fromStream(
            clientId = clientId,
            scope = configGlobalScope,
            configRepository.findActive(clientId, configGlobalScope),
            emptyList(),
        )
    }

    fun putGlobalConfig(config: ClientConfig, clientId: String) {
        configRepository.update(clientId, config.toVariableSet(configGlobalScope))
    }

    fun projectConfig(clientId: String, projectId: String): ClientConfig {
        val scope = projectScope(projectId).configScope
        return ClientConfig.fromStream(
            clientId = clientId,
            scope = scope,
            config = configRepository.findActive(clientId, scope),
            defaults = listOfNotNull(configRepository.findActive(clientId, configGlobalScope)),
        )
    }


    fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig) {
        configRepository.update(clientId, clientConfig.toVariableSet(projectScope(projectId).configScope))
    }

    fun putUserConfig(clientId: String, userId: String, clientConfig: ClientConfig) {
        configRepository.update(clientId, clientConfig.toVariableSet(userScope(userId).configScope))
    }

    fun userConfig(clientId: String, projectId: String, userId: String): ClientConfig {
        val scope = userScope(userId).configScope
        val defaultScopes = listOf(projectScope(projectId).configScope, configGlobalScope)
        return ClientConfig.fromStream(
            clientId = clientId,
            scope = scope,
            config = configRepository.findActive(clientId, scope),
            defaults = defaultScopes.mapNotNull { configRepository.findActive(clientId, it) },
        )
    }

    companion object {
        private val Scope.configScope: Scope
            get() = prefixWith("config")

        val globalScope: Scope = SimpleScope("global")
        fun projectScope(projectId: String): Scope = SimpleScope(QualifiedId(listOf("project", projectId)))
        fun userScope(userId: String): Scope = SimpleScope(QualifiedId("user", userId))

        private val configGlobalScope = globalScope.configScope
    }
}
