package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.config.ConditionScope
import org.radarbase.appconfig.config.ProjectScope
import org.radarbase.appconfig.config.Scopes.GLOBAL_CONFIG_SCOPE
import org.radarbase.appconfig.config.Scopes.config
import org.radarbase.appconfig.config.UserScope
import org.radarbase.appconfig.persistence.ConfigRepository

class ConfigService(
    @Context private val configRepository: ConfigRepository,
    @Context private val conditionService: ConditionService,
) {
    fun globalConfig(clientId: String): ClientConfig {
        return ClientConfig.fromStream(
            clientId = clientId,
            scope = GLOBAL_CONFIG_SCOPE,
            configRepository.findActive(clientId, GLOBAL_CONFIG_SCOPE),
            emptyList(),
        )
    }

    fun putGlobalConfig(config: ClientConfig, clientId: String) {
        configRepository.update(clientId, config.toVariableSet(GLOBAL_CONFIG_SCOPE))
    }

    fun projectConfig(clientId: String, projectId: String): ClientConfig {
        val scope = ProjectScope(projectId).config
        return ClientConfig.fromStream(
            clientId = clientId,
            scope = scope,
            config = configRepository.findActive(clientId, scope),
            defaults = listOfNotNull(configRepository.findActive(clientId, GLOBAL_CONFIG_SCOPE)),
        )
    }

    fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig) {
        configRepository.update(clientId, clientConfig.toVariableSet(ProjectScope(projectId).config))
    }

    fun putUserConfig(clientId: String, userId: String, clientConfig: ClientConfig) {
        configRepository.update(clientId, clientConfig.toVariableSet(UserScope(userId).config))
    }

    fun conditionConfig(clientId: String, projectId: String, conditionName: String): ClientConfig {
        val projectScope = ProjectScope(projectId)
        val scope = ConditionScope(conditionName, projectScope).config
        val defaultScopes = listOf(
            projectScope.config,
            GLOBAL_CONFIG_SCOPE,
        )

        return ClientConfig.fromStream(
            clientId = clientId,
            scope = scope,
            config = configRepository.findActive(clientId, scope),
            defaults = defaultScopes.mapNotNull { configRepository.findActive(clientId, it) },
        )
    }

    fun setConditionConfig(
        clientId: String,
        projectId: String,
        conditionName: String,
        clientConfig: ClientConfig,
    ) = configRepository.update(clientId, clientConfig.toVariableSet(ConditionScope(conditionName, projectId)))

    fun userConfig(clientId: String, projectId: String, userId: String): ClientConfig {
        val scope = UserScope(userId).config
        val defaultScopes = conditionService.matchingScopes(clientId, projectId, userId).map { it.config } +
            listOf(
                ProjectScope(projectId).config,
                GLOBAL_CONFIG_SCOPE,
            )
        return ClientConfig.fromStream(
            clientId = clientId,
            scope = scope,
            config = configRepository.findActive(clientId, scope),
            defaults = defaultScopes.mapNotNull { configRepository.findActive(clientId, it) },
        )
    }
}
