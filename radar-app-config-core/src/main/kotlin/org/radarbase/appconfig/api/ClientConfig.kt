package org.radarbase.appconfig.api

import nl.thehyve.lang.expression.ResolvedVariable
import nl.thehyve.lang.expression.Scope
import java.util.stream.Collectors
import java.util.stream.Stream

data class ClientConfig(
    val clientId: String?,
    val scope: String?,
    val config: List<SingleVariable>,
    val defaults: List<SingleVariable>? = null
) {
    companion object {
        fun fromStream(clientId: String, scope: Scope, configStream: Stream<ResolvedVariable>): ClientConfig {
            val configs = configStream.collect(Collectors.groupingBy { it.scope == scope })
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

    fun copyWithConfig(config: Map<String, String>): ClientConfig {
        val existingConfig = this.config.stream();
        val configUpdate = config.entries.stream()
            .map { e -> SingleVariable(e.key, e.value, null) }

        val newConfig = Stream.concat(existingConfig, configUpdate)
            .collect(
                Collectors.groupingBy(
                    SingleVariable::name, ::LinkedHashMap,
                    Collectors.reducing(null) { _, b -> b }))
            .values
            .filterNotNull();
        return ClientConfig(clientId, scope, newConfig, defaults);
    }
}
