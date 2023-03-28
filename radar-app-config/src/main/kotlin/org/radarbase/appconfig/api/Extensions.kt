package org.radarbase.appconfig.api

import org.radarbase.management.client.MPOAuthClient
import org.radarbase.management.client.MPProject
import org.radarbase.management.client.MPSubject

fun MPProject.toProject(): Project = Project(
    name = id,
    humanReadableName = name,
    location = location,
    organization = organization?.id,
    description = description,
)

fun MPSubject.toUser(): User = User(
    id = requireNotNull(id),
    externalUserId = externalId,
)

fun MPOAuthClient.toOAuthClient() = OAuthClient(id = id)
