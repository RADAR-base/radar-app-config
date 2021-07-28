package org.radarbase.appconfig.inject

import jakarta.ws.rs.core.Context
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
            var variable = configRepository.findActiveValue(clientId, scopes, id)

            val remainingScopes = if (variable != null) {
                scopes.subList(0, scopes.indexOf(variable.scope))
            } else scopes

            val projectVariable = resolveProjectVariables(remainingScopes, id)
            if (projectVariable != null) {
                variable = projectVariable
            }

            return variable
                ?: throw NoSuchElementException("Unknown variable $id in scopes $scopes.")
        }

        private fun resolveProjectVariables(scopes: List<Scope>, id: QualifiedId): ResolvedVariable? {
            val dynamicProjectIds = scopes
                .filter { scope -> scope.isPrefixedBy("dynamic.project") && scope.id.names.size == 3 }
                .map { it.id.names[2] to it }

            if (dynamicProjectIds.isEmpty()) return null

            val (type, idSecond) = id.splitHead()
            if (type == "user" && idSecond != null) {
                val (userId, attribute) = idSecond.splitHead()
                if (userId != null && attribute != null) {
                    dynamicProjectIds
                        .firstNotNullOfOrNull { (projectId, scope) ->
                            projectService.getUser(projectId, userId)
                                ?.let { subject -> scope to subject }
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
