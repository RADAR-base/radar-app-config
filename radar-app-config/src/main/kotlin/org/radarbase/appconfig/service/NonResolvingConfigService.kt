package org.radarbase.appconfig.service

import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.ResolvedVariable
import org.radarbase.lang.expression.Scope

interface NonResolvingConfigService {
    suspend fun getConfig(
        clientId: String,
        scopes: Collection<Scope>? = null,
        id: QualifiedId? = null,
        prefix: QualifiedId? = null,
        version: Int? = null,
    ): Sequence<ResolvedVariable>
}
