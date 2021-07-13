package org.radarbase.appconfig.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import nl.thehyve.lang.expression.*
import org.radarbase.appconfig.persistence.entity.ConfigStateEntity
import org.radarbase.management.client.MPOAuthClient
import org.radarbase.management.client.MPProject
import org.radarbase.management.client.MPSubject
import java.time.Instant

data class ProjectList(val projects: Collection<Project>)

data class Project(
    @JsonProperty("projectName") val name: String,
    @JsonProperty("humanReadableProjectName") val humanReadableName: String? = null,
    val location: String? = null,
    val organization: String? = null,
    val description: String? = null
)

fun MPProject.toProject(): Project = Project(
    name = id,
    humanReadableName = name,
    location = location,
    organization = organization,
    description = description
)

data class UserList(val users: Collection<User>)
data class User(
    val id: String,
    val externalUserId: String? = null,
    val hasConfig: Boolean? = null,
)

fun MPSubject.toUser(): User = User(
    id = requireNotNull(id),
    externalUserId = externalId,
)

data class OAuthClient(@JsonProperty("clientId") val id: String)

fun MPOAuthClient.toOAuthClient() = OAuthClient(id = id)

data class OAuthClientList(val clients: List<OAuthClient>)

data class ConditionList(val conditions: List<Condition>)

data class Condition(
    val id: Long?,
    val name: String?,
    val title: String? = null,
    val expression: Expression,
    val config: Map<String, Map<String, String>>? = null
)

data class ClientConfig(
    val clientId: String?,
    val scope: String?,
    val config: List<SingleVariable>,
    val defaults: List<SingleVariable>? = null,
    val lastModifiedAt: Instant? = null,
) {
    fun toVariableSet(scope: Scope, overrideLastModified: Boolean = true) = VariableSet(
        type = ConfigStateEntity.Type.CONFIG.name,
        scope = scope,
        variables = config.associate { (name, value, _) -> QualifiedId(name) to value.toVariable() },
        lastModifiedAt = if (overrideLastModified) Instant.now() else lastModifiedAt,
    )

    companion object {
        fun fromStream(clientId: String, scope: Scope, config: VariableSet?, defaults: List<VariableSet>): ClientConfig {
            val lastModified = maxOf(
                config?.lastModifiedAt ?: Instant.MIN,
                defaults.maxOfOrNull { it.lastModifiedAt ?: Instant.MIN } ?: Instant.MIN
            )

            val scopeConfig = config?.variables
                ?.map { (id, value) -> SingleVariable(id.asString(), value.asOptString()) }
                ?: listOf()

            val defaultConfig = mutableMapOf<String, SingleVariable>()
            for (variableSet in defaults) {
                for ((id, value) in variableSet.variables) {
                    val name = id.asString()
                    if (name !in defaultConfig) {
                        defaultConfig[name] = SingleVariable(name, value.asOptString(), variableSet.scope.asString())
                    }
                }
            }

            return ClientConfig(
                clientId = clientId,
                scope = scope.asString(),
                config = scopeConfig,
                defaults = defaultConfig.takeIf { it.isNotEmpty() }?.values?.toList(),
                lastModifiedAt = lastModified.takeUnless { it == Instant.MIN }
            )
        }
    }
}

data class SingleVariable(
    val name: String,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val value: String?,
    val scope: String? = null,
)

data class ClientProtocol(
    val clientId: String,
    val scope: String?,
    val contents: JsonNode,
    val lastModifiedAt: Instant,
)
