package org.radarbase.appconfig.service

import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope

interface NonResolvingConfigService {
    suspend fun getConfig(
        clientId: String,
        scopes: Collection<Scope>? = null,
        name: QualifiedId? = null,
        prefix: QualifiedId? = null,
        version: Int? = null,
    ): ClientConfig
}
