package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.persistence.entity.ConfigStateEntity
import java.time.Instant

class ConfigService(
    @Context private val resolver: ClientVariableResolver,
) {
    fun globalConfig(clientId: String): ClientConfig {
        return ClientConfig.fromStream(
            clientId = clientId,
            scope = globalScope,
            resolver[clientId].resolve(ConfigStateEntity.Type.CONFIG.name, globalScope),
            emptyList(),
        )
    }

    fun putGlobalConfig(config: ClientConfig, clientId: String) {
        resolver[clientId].replace(config.toVariableSet(globalScope))
    }

    companion object {
        val globalScope: Scope = SimpleScope("global")
        fun userScope(userId: String): Scope = SimpleScope(QualifiedId("user", userId))
    }
}
