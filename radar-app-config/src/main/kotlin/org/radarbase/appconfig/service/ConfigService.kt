package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.lang.expression.NullLiteral
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.SimpleScope
import org.radarbase.lang.expression.toVariable
import org.radarbase.appconfig.persistence.HibernateVariableResolver

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

    suspend fun globalConfigName(clientId: String, name: String): ClientConfig {
        return ClientConfig.fromResolvedVariable(
            clientId,
            globalScope,
            resolver[clientId].resolve(listOf(globalScope), QualifiedId(name))
        )
    }

    suspend fun globalConfigNameVersion(clientId: String, name: String, version: Int): ClientConfig {
        return ClientConfig.fromVersionStream(
            clientId,
            globalScope,
            resolver[clientId].resolveVersion(listOf(globalScope), QualifiedId(name), version)
        )
    }

    suspend fun globalConfigNameVersions(clientId: String, name: String): List<ClientConfig> {
        val sequence = resolver[clientId].resolveVersions(listOf(globalScope), QualifiedId(name))
        val config = ClientConfig.fromVersionStream(clientId, globalScope, sequence)
        return listOf(config)
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
