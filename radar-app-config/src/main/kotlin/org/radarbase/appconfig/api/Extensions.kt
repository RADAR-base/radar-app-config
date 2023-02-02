package org.radarbase.appconfig.api

import org.radarbase.appconfig.persistence.entity.ConditionEntity
import org.radarbase.lang.expression.ExpressionParser
import org.radarbase.management.client.MPOAuthClient
import org.radarbase.management.client.MPProject
import org.radarbase.management.client.MPSubject

fun MPProject.toProject(): Project = Project(
    name = id,
    humanReadableName = name,
    location = location,
    organization = organization,
    description = description
)

fun MPSubject.toUser(): User = User(
    id = requireNotNull(id),
    externalUserId = externalId,
)

fun MPOAuthClient.toOAuthClient() = OAuthClient(id = id)

fun ConditionEntity.toCondition(parser: ExpressionParser) = Condition(
    id = id,
    name = name,
    title = title,
    expression = expression?.let { parser.parse(it) },
    rank = rank,
    lastModifiedAt = lastModifiedAt,
)
