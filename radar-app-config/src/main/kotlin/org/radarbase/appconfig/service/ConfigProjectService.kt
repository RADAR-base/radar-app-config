package org.radarbase.appconfig.service

import org.radarbase.appconfig.domain.GlobalConfig
import org.radarbase.appconfig.domain.Project

interface ConfigProjectService {
    fun listProjects(): Set<Project>
    fun projectConfig(projectId: String): GlobalConfig
    fun putConfig(projectId: String, globalConfig: GlobalConfig)
}