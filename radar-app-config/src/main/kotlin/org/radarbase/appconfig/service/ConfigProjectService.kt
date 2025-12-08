package org.radarbase.appconfig.service

import org.radarbase.appconfig.api.ClientConfig

interface ConfigProjectService {
    suspend fun getProjectConfig(clientId: String, projectId: String): ClientConfig?
    suspend fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig)
    suspend fun getProjectConfigByName(projectId: String, clientId: String, name: String): ClientConfig?
    suspend fun getProjectConfigByNameAndAllVersions(projectId: String, clientId: String, name: String): List<ClientConfig>
    suspend fun getProjectConfigByNameAndVersion(projectId: String, clientId: String, name: String, version: Int): ClientConfig?
}
