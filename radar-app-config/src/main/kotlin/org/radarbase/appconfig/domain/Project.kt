package org.radarbase.appconfig.domain

import com.fasterxml.jackson.annotation.JsonProperty
import nl.thehyve.lang.expression.Expression
import nl.thehyve.lang.expression.ResolvedVariable
import nl.thehyve.lang.expression.Scope
import java.util.stream.Collectors
import java.util.stream.Stream

data class ProjectList(val projects: Collection<Project>)

data class Project(val id: Long, @JsonProperty("projectName") val name: String, @JsonProperty("humanReadableProjectName") val humanReadableName: String? = null, val location: String? = null, val organization: String? = null, val description: String? = null)

data class UserList(val users: Collection<User>)
data class User(val id: String, val externalUserId: String? = null, val hasConfig: Boolean? = null)
data class MPUser(val login: String, val externalUserId: String? = null)

data class OAuthClient(@JsonProperty("clientId") val id: String)

data class OAuthClientList(val clients: List<OAuthClient>)

data class ConditionList(val conditions: List<Condition>)

data class Condition(val id: Long?, val name: String?, val title: String? = null, val expression: Expression, val config: Map<String, Map<String, String>>? = null)

data class ClientConfig(val clientId: String?, val scope: String?, val config: List<SingleVariable>, val defaults: List<SingleVariable>? = null) {
    companion object {
        fun fromStream(clientId: String, scope: Scope, configStream: Stream<ResolvedVariable>): ClientConfig {
            val configs = configStream.collect(Collectors.groupingBy<ResolvedVariable, Boolean> { it.scope == scope })
            return ClientConfig(clientId, scope.asString(),
                    configs[true]
                            ?.map { (_, id, variable) ->
                                SingleVariable(id.asString(), variable.asOptString())
                            }
                            ?: listOf(),
                    configs[false]
                            ?.map { (scope, id, variable) ->
                                SingleVariable(id.asString(), variable.asOptString(), scope.asString())
                            })
        }
    }
}

data class SingleVariable(val name: String, val value: String?, val scope: String? = null)