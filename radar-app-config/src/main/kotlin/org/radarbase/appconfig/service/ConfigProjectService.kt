package org.radarbase.appconfig.service

import org.radarbase.appconfig.domain.ClientConfig

interface ConfigProjectService {
    fun projectConfig(clientId: String, projectId: String): ClientConfig
    fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig)
}
