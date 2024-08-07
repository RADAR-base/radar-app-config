package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.lang.expression.NullLiteral
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.SimpleScope
import org.radarbase.lang.expression.toVariable

class ConfigService(
    @Context private val resolver: ClientVariableResolver,
    @Context private val conditionService: ConditionService,
    @Context private val clientService: ClientService,
) {
    suspend fun globalConfig(clientId: String): ClientConfig {
        return ClientConfig.fromStream(
            clientId,
            globalScope,
            resolver[clientId].resolveAll(listOf(globalScope), null),
        )
    }

    suspend fun putGlobalConfig(config: ClientConfig, clientId: String) {
        resolver[clientId].replace(
            globalScope,
            null,
            config.config.asSequence()
                .map { (innerId, variable, _) ->
                    QualifiedId(innerId) to
                        (variable?.toVariable() ?: NullLiteral())
                },
        )
    }

    companion object {
        val globalScope: Scope = SimpleScope("global")
        fun userScope(userId: String): Scope = SimpleScope(QualifiedId("user", userId))
    }
}
