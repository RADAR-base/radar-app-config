package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.SingleVariable
import org.radarbase.appconfig.inject.ClientVariableRepository
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope

class NonResolvingConfigServiceImpl(
    @Context private val clientVariableRepository: ClientVariableRepository,
) : NonResolvingConfigService {
    override suspend fun getConfig(
        clientId: String,
        scopes: Collection<Scope>?,
        name: QualifiedId?,
        prefix: QualifiedId?,
        version: Int?,
    ) = ClientConfig(
        clientId = clientId,
        config = clientVariableRepository[clientId]
            .query(scopes, name, prefix, version)
            .map { it.toSingleVariable() }
            .toList()
    )
}

internal fun ConfigEntity.toSingleVariable(): SingleVariable {
    return SingleVariable(
        name, value, scope, clientId, version, createdByUser, createTimestamp?.toEpochMilli(),
    )
}
