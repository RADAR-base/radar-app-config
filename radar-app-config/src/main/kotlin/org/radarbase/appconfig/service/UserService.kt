package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.persistence.HibernateVariableResolver
import org.radarbase.appconfig.service.ConditionService.Companion.conditionScope
import org.radarbase.appconfig.service.ConfigProjectServiceImpl.Companion.projectScope
import org.radarbase.appconfig.service.ConfigService.Companion.globalScope
import org.radarbase.appconfig.service.ConfigService.Companion.userScope
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.lang.expression.NullLiteral
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.toVariable

class UserService(
    @Context private val conditionService: ConditionService,
    @Context private val resolver: ClientVariableResolver,
) {
    suspend fun putUserConfig(clientId: String, userId: String, clientConfig: ClientConfig) {
        resolver[clientId].replace(
            userScope(userId),
            null,
            clientConfig.config.asSequence()
                .map { (innerId, value, _) ->
                    Pair(QualifiedId(innerId), value?.toVariable() ?: NullLiteral())
                },
        )
    }

    suspend fun userConfig(
        clientId: String,
        projectId: String,
        userId: String,
    ): ClientConfig {
        val scopes = userScopes(clientId, projectId, userId)
        return ClientConfig.fromStream(
            clientId,
            scopes[0],
            resolver[clientId].resolveAll(scopes, null),
        )
    }

    /**
     * Return the most recent config with the given name for this user. It searches in order
     * of specificity: user -> matching conditions -> project -> global.
     */
    suspend fun userConfigName(
        clientId: String,
        projectId: String,
        userId: String,
        name: String,
    ): ClientConfig? {
        val vr = resolver[clientId]
        if (vr !is HibernateVariableResolver) return null

        val scopes = userScopes(clientId, projectId, userId)
        val qName = QualifiedId(name)
        for (scope in scopes) {
            val cfg = vr.versions(scope, qName).firstOrNull()
            if (cfg != null) return cfg
        }
        return null
    }

    /**
     * Return all versions of the config with the given name in the user scope only.
     */
    suspend fun userConfigNameVersions(
        clientId: String,
        userId: String,
        name: String,
    ): List<ClientConfig> {
        val vr = resolver[clientId]
        return if (vr is HibernateVariableResolver) {
            vr.versions(userScope(userId), QualifiedId(name))
        } else {
            emptyList()
        }
    }

    /**
     * Return the specific version of the config with the given name in the user scope only,
     * or throw if not found.
     */
    suspend fun userConfigNameVersion(
        clientId: String,
        userId: String,
        name: String,
        version: Int,
    ): ClientConfig {
        val vr = resolver[clientId]
        val versions = if (vr is HibernateVariableResolver) {
            vr.versions(userScope(userId), QualifiedId(name))
        } else emptyList()

        return versions.firstOrNull { it.config.firstOrNull()?.version == version }
            ?: throw HttpNotFoundException(
                "config_version_not_found",
                "No config found for name '$name' with version $version in scope '${userScope(userId).asString()}' for client $clientId.",
            )
    }

    private suspend fun userScopes(
        clientId: String,
        projectId: String,
        userId: String,
    ): List<Scope> = buildList {
        add(userScope(userId))
        conditionService.matchingConditions(clientId, projectId, userId)
            .forEach { add(conditionScope(it)) }
        add(projectScope(projectId))
        add(globalScope)
    }
}
