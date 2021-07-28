package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.managementportal.RadarProjectService
import org.radarbase.management.client.MPProject
import org.radarbase.management.client.MPSubject

class FixedRadarProjectService(
    @Context config: ApplicationConfig,
) : RadarProjectService {
    private val projects = config.projects

    override fun ensureUser(projectId: String, userId: String) {
        ensureProject(projectId)
        if (userId !in projects[projectId]!!) throw HttpNotFoundException("user_not_found", "User $userId not found in project $projectId")
    }

    override fun getUser(projectId: String, userId: String): MPSubject? {
        val externalId = projects[projectId]?.get(userId) ?: return null
        return MPSubject(id = userId, projectId = projectId, externalId = externalId, project = project(projectId))
    }

    override fun project(projectId: String): MPProject {
        ensureProject(projectId)
        return MPProject(id = projectId, name = projectId)
    }

    override fun ensureProject(projectId: String) {
        if (projectId !in projects) throw HttpNotFoundException("project_not_found", "Project $projectId not found.")
    }

    override fun projectUsers(projectId: String): List<MPSubject> {
        val project = project(projectId)
        return projects[projectId]!!.map { (userId, externalId) ->
            MPSubject(id = userId, projectId = projectId, externalId = externalId, project = project)
        }
    }

    override fun userByExternalId(projectId: String, externalUserId: String): MPSubject? {
        return projectUsers(projectId).find { it.externalId == externalUserId }
    }

    override fun userProjects(auth: Auth, permission: Permission): List<MPProject> {
        return projects.keys.map { MPProject(id = it, name = it) }
    }
}
