package org.radarbase.appconfig.api

import kotlinx.serialization.Serializable
import org.radarbase.lang.expression.ResolvedVariable
import org.radarbase.lang.expression.Scope

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
                    ?.map { (_, id, variable, createTimestamp, createdBy, version) ->
                        SingleVariable(id.asString(), variable.asOptString(),
                            scope.asString(), clientId, version, createdBy, createTimestamp?.toEpochMilli()
                        )
                    }
                    ?: emptyList(),
                configs[false]
                    ?.map { (scope, id, variable, createTimestamp, createdBy, version) ->
                        SingleVariable(id.asString(), variable.asOptString(),
                            scope.asString(), clientId, version, createdBy, createTimestamp?.toEpochMilli()
                        )
                    },
            )
        }

        fun fromResolvedVariable(
            clientId: String,
            scope: Scope,
            resolvedConfig: ResolvedVariable,
        ): ClientConfig {
            val (resolvedScope, id, variable, createTimestamp, createdBy, version) = resolvedConfig
            val single = SingleVariable(
                id.asString(),
                variable.asOptString(),
                resolvedScope.asString(),
                clientId,
                version,
                createdBy,
                createTimestamp?.toEpochMilli(),
            )
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
            val entries = configSequence.map { (resolvedScope, id, variable, createTimestamp, createdBy, version) ->
                SingleVariable(
                    id.asString(),
                    variable.asOptString(),
                    resolvedScope.asString(),
                    clientId,
                    version,
                    createdBy,
                    createTimestamp?.toEpochMilli(),
                )
            }.toList()
            return ClientConfig(
                clientId,
                scope.asString(),
                entries,
                null,
            )
        }
    }
}
