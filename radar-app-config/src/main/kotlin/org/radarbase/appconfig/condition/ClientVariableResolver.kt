package org.radarbase.appconfig.condition

import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.config.ConfigScope
import org.radarbase.appconfig.config.DynamicScope
import org.radarbase.appconfig.config.Scopes.dynamic
import org.radarbase.appconfig.config.Scopes.toAppConfigScope
import org.radarbase.appconfig.config.UserScope
import org.radarbase.appconfig.persistence.ConfigRepository
import org.radarbase.jersey.service.managementportal.RadarProjectService
import org.radarbase.lang.expression.*

class ClientVariableResolver(
    @Context private val configRepository: ConfigRepository,
    @Context private val projectService: RadarProjectService,
) {
    operator fun get(clientId: String): VariableResolver = DelegatingVariableResolver(clientId)

    inner class DelegatingVariableResolver(
        private val clientId: String
    ) : VariableResolver {
        override fun resolve(scopes: List<Scope>, id: QualifiedId): ResolvedVariable {
            val appScopes = scopes.map { it.toAppConfigScope() }

            var variable = configRepository.findActiveValue(clientId, appScopes.filterIsInstance<ConfigScope>(), id)

            val remainingScopes = if (variable != null) {
                appScopes.subList(0, appScopes.indexOf(variable.scope))
            } else scopes

            val projectVariable = resolveDynamicVariables(remainingScopes.filterIsInstance<DynamicScope>(), id)
            if (projectVariable != null) {
                variable = projectVariable
            }

            return variable
                ?: throw NoSuchElementException("Unknown variable $id in scopes $scopes.")
        }

        private fun resolveDynamicVariables(scopes: List<DynamicScope>, id: QualifiedId): ResolvedVariable? {
            val userScopes = scopes
                .map { it.subScope }
                .filterIsInstance<UserScope>()
                .filter { it.projectScope != null }

            val (type, idSecond) = id.splitHead()
            if (type == "user" && idSecond != null) {
                val (userId, attribute) = idSecond.splitHead()
                if (userId != null && attribute != null) {
                    userScopes
                        .firstNotNullOfOrNull { userScope ->
                            projectService.getUser(userScope.projectScope!!.projectId, userId)
                                ?.let { subject -> userScope.dynamic to subject }
                        }
                        ?.let { (scope, subject) ->
                            return ResolvedVariable(
                                scope,
                                id,
                                subject.attributes[attribute.asString()].toVariable(),
                            )
                        }
                }
            }
            return null
        }
    }
}
