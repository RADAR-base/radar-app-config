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
                    ?.map { (_, id, variable) ->
                        SingleVariable(id.asString(), variable.asOptString())
                    }
                    ?: emptyList(),
                configs[false]
                    ?.map { (scope, id, variable) ->
                        SingleVariable(id.asString(), variable.asOptString(), scope.asString())
                    },
            )
        }
    }
}
