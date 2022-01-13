package org.radarbase.appconfig.api

import org.radarbase.lang.expression.ResolvedVariable
import org.radarbase.lang.expression.Scope
import java.util.stream.Collectors

data class ClientConfig(
    val clientId: String?,
    val scope: String?,
    val config: List<SingleVariable>,
    val defaults: List<SingleVariable>? = null
) {
    companion object {
        fun fromStream(
            clientId: String,
            scope: Scope,
            configSequence: Sequence<ResolvedVariable>
        ): ClientConfig {
            val configs = configSequence.groupBy { it.scope == scope }
            return ClientConfig(
                clientId,
                scope.asString(),
                configs[true]
                    ?.map { (_, id, variable) ->
                        SingleVariable(id.asString(), variable.asOptString())
                    }
                    ?: listOf(),
                configs[false]
                    ?.map { (scope, id, variable) ->
                        SingleVariable(id.asString(), variable.asOptString(), scope.asString())
                    },
            )
        }
    }

    fun copyWithConfig(config: Map<String, String>): ClientConfig {
        val existingConfig = this.config.asSequence()
        val configUpdate = config.entries.asSequence()
            .map { (k, v) -> SingleVariable(k, v, null) }

        val newConfig = (existingConfig + configUpdate)
            .associateBy { (k) -> k }
            .values
            .toList()

        return ClientConfig(clientId, scope, newConfig, defaults);
    }
}
