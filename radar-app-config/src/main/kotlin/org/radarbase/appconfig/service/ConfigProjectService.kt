package org.radarbase.appconfig.service

import org.radarbase.appconfig.api.ClientConfig

interface ConfigProjectService {
    suspend fun projectConfig(clientId: String, projectId: String): ClientConfig
    suspend fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig)
}
