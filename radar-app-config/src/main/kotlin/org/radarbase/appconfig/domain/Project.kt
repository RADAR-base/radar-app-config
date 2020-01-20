package org.radarbase.appconfig.domain

import com.fasterxml.jackson.annotation.JsonProperty
import nl.thehyve.lang.expression.Expression
import nl.thehyve.lang.expression.ResolvedVariable
import java.util.stream.Collectors
import java.util.stream.Stream

data class ProjectList(val projects: List<Project>)

data class Project(val id: Long, @JsonProperty("projectName") val name: String, @JsonProperty("humanReadableProjectName") val humanReadableName: String? = null, val location: String? = null, val organization: String? = null, val description: String? = null)

data class OAuthClient(@JsonProperty("clientId") val id: String)

data class OAuthClientList(val clients: List<OAuthClient>)

data class ConditionList(val conditions: List<Condition>)

data class Condition(val id: Long?, val name: String?, val title: String? = null, val expression: Expression, val config: Map<String, Map<String, String>>? = null)

data class ClientConfig(val clientId: String?, val config: List<SingleVariable>) {
    companion object {
        fun fromStream(clientId: String, stream: Stream<ResolvedVariable>): ClientConfig {
            return ClientConfig(clientId, stream
                    .map { (scope, id, variable) ->
                        SingleVariable(id.asString(), variable.asOptString(), scope.asString())
                    }
                    .collect(Collectors.toList()))
        }
    }
}

data class SingleVariable(val name: String, val value: String?, val scope: String? = null)