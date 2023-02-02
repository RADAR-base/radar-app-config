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
            if (id.size < 3 || id.names[0] != "user") return null
            val userId = id[1]
            val attribute = id.tail(2)

            return scopes
                .asSequence()
                .flatMap { it.scopes() }
                .filterIsInstance<UserScope>()
                .firstNotNullOfOrNull { userScope ->
                    val projectScope = userScope.projectScope ?: return@firstNotNullOfOrNull null
                    val subject = projectService.getUser(projectScope.projectId, userId) ?: return@firstNotNullOfOrNull null
                    ResolvedVariable(
                        userScope.dynamic,
                        id,
                        subject.attributes[attribute.asString()].toVariable(),
                    )
                }
        }
    }
}
