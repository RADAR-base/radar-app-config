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

    suspend fun globalConfig(clientId: String): List<ClientConfig> {
        val vr = resolver[clientId]
        return if (vr is HibernateVariableResolver) {
            vr.mostRecentConfigs(globalScope)
        } else {
            emptyList()
        }
    }

    suspend fun globalConfigName(clientId: String, name: String): ClientConfig? {
        val vr = resolver[clientId]
        return if (vr is HibernateVariableResolver) {
            vr.versions(globalScope, QualifiedId(name)).firstOrNull()
        } else {
            null
        }
    }

    suspend fun globalConfigNameVersion(clientId: String, name: String, version: Int): ClientConfig {
        val vr = resolver[clientId]
        val versions = if (vr is HibernateVariableResolver) {
            vr.versions(globalScope, QualifiedId(name))
        } else emptyList()

        return versions.firstOrNull { it.config.firstOrNull()?.version == version }
            ?: throw HttpNotFoundException(
                "config_version_not_found",
                "No config found for name '$name' with version $version in scope '${globalScope.asString()}' for client $clientId.",
            )
    }

    suspend fun globalConfigNameVersions(clientId: String, name: String): List<ClientConfig> {
        val vr = resolver[clientId]
        return if (vr is HibernateVariableResolver) {
            vr.versions(globalScope, QualifiedId(name))
        } else {
            emptyList()
        }
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
