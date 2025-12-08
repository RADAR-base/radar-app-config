package org.radarbase.appconfig.api

import kotlinx.serialization.Serializable
import org.radarbase.lang.expression.ResolvedVariable
import org.radarbase.lang.expression.Scope

// Extension function to convert a ResolvedVariable into a SingleVariable for a given clientId.
internal fun ResolvedVariable.toSingleVariable(clientId: String): SingleVariable {
    val (resolvedScope, id, variable, createTimestamp, createdBy, version) = this
    return SingleVariable(
        id.asString(),
        variable.asOptString(),
        resolvedScope.asString(),
        clientId,
        version,
        createdBy,
        createTimestamp?.toEpochMilli(),
    )
}

@Serializable
data class ClientConfig(
    val clientId: String?,
    val scope: String?,
    val config: List<SingleVariable>,
    val defaults: List<SingleVariable>? = null,
) {
    fun with(other: ClientConfig): ClientConfig = ClientConfig(
        clientId = clientId,
        scope = scope,
        config = (config.asSequence() + other.config.asSequence())
            .associateBy { (k) -> k }
            .values
            .toList(),
        defaults = defaults,
    )

    companion object {
        fun fromStream(
            clientId: String,
            scope: Scope,
            configSequence: Sequence<ResolvedVariable>,
        ): ClientConfig {
            val configs = configSequence.groupBy { it.scope == scope }
            return ClientConfig(
                clientId,
                scope.asString(),
                configs[true]
                    ?.map { it.toSingleVariable(clientId) }
                    ?: emptyList(),
                configs[false]
                    ?.map { it.toSingleVariable(clientId) },
            )
        }

        fun fromResolvedVariable(
            clientId: String,
            scope: Scope,
            resolvedConfig: ResolvedVariable,
        ): ClientConfig {
            val single = resolvedConfig.toSingleVariable(clientId)
            return ClientConfig(
                clientId,
                scope.asString(),
                listOf(single),
                emptyList(),
            )
        }

        fun fromVersionStream(
            clientId: String,
            scope: Scope,
            configSequence: Sequence<ResolvedVariable>,
        ): ClientConfig {
            val entries = configSequence.map { it.toSingleVariable(clientId) }.toList()
            return ClientConfig(
                clientId,
                scope.asString(),
                entries,
                null,
            )
        }
    }
}
