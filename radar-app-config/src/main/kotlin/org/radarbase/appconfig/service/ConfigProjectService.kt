package org.radarbase.appconfig.service

import org.radarbase.appconfig.api.ClientConfig

interface ConfigProjectService {
    suspend fun projectConfig(clientId: String, projectId: String): ClientConfig?
    suspend fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig)
    suspend fun projectConfigName(projectId: String, clientId: String, name: String): ClientConfig?
    suspend fun projectConfigNameVersions(projectId: String, clientId: String, name: String): List<ClientConfig>
    suspend fun projectConfigNameVersion(projectId: String, clientId: String, name: String, version: Int): ClientConfig?
}
