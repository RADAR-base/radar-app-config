package org.radarbase.appconfig.service

import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.Project

interface ConfigProjectService {
    fun listProjects(): Set<Project>
    fun projectConfig(clientId: String, projectId: String): ClientConfig
    fun putProjectConfig(clientId: String, projectId: String, clientConfig: ClientConfig)
    fun userConfig(clientId: String, projectId: String, userId: String): ClientConfig
    fun putUserConfig(clientId: String, userId: String, clientConfig: ClientConfig)
}
