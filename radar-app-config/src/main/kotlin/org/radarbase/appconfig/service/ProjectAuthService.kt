package org.radarbase.appconfig.service

import org.radarbase.jersey.auth.ProjectService
import org.radarbase.jersey.exception.HttpNotFoundException
import javax.ws.rs.core.Context

class ProjectAuthService(
        @Context private val configProjectService: ConfigProjectService
): ProjectService {
    override fun ensureProject(projectId: String) {
        if (projectId !in configProjectService) {
            throw HttpNotFoundException("project_not_found", "Project $projectId not found.")
        }
    }
}
