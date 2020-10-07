package org.radarbase.appconfig.service

import org.radarbase.appconfig.domain.User
import org.radarbase.appconfig.domain.toUser
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.managementportal.RadarProjectService

fun RadarProjectService.ensureUser(projectId: String, userId: String) {
    if (projectUsers(projectId).find { it.id == userId } == null) {
        throw HttpNotFoundException("user_missing", "User $userId not found in project $projectId")
    }
}

fun RadarProjectService.find(projectId: String, userId: String): User? = projectUsers(projectId)
        .find { it.id == userId }
        ?.toUser()
