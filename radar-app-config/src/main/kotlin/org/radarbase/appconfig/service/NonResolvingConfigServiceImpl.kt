package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.inject.ClientVariableRepository
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope

class NonResolvingConfigServiceImpl(
    @Context private val clientVariableRepository: ClientVariableRepository,
) : NonResolvingConfigService {
    override suspend fun getConfig(
        clientId: String,
        scopes: Collection<Scope>?,
        id: QualifiedId?,
        prefix: QualifiedId?,
        version: Int?
    ) = clientVariableRepository[clientId].query(scopes, id, prefix, version)
}
