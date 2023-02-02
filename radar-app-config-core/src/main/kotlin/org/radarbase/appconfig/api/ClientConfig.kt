package org.radarbase.appconfig.api

import java.time.Instant
import org.radarbase.lang.expression.*

data class ClientConfig(
    val clientId: String?,
    val scope: String?,
    val config: List<SingleVariable>,
    val defaults: List<SingleVariable>? = null,
    val lastModifiedAt: Instant? = null,
) {
    fun toVariableSet(scope: Scope, overrideLastModified: Boolean = false) = VariableSet(
        id = null,
        scope = scope,
        variables = config.associate { (name, value, _) -> QualifiedId(name) to value.toVariable() },
        lastModifiedAt = if (overrideLastModified) Instant.now() else lastModifiedAt,
    )

    fun with(other: ClientConfig): ClientConfig = ClientConfig(
        clientId = clientId,
        scope = scope,
        config = (config.asSequence() + other.config.asSequence())
            .associateBy { (k) -> k }
            .values
            .toList(),
        defaults = defaults
    )

    companion object {
        fun fromStream(clientId: String, scope: Scope, config: VariableSet?, defaults: List<VariableSet>): ClientConfig {
            val lastModified = maxOf(
                config?.lastModifiedAt ?: Instant.MIN,
                defaults.maxOfOrNull { it.lastModifiedAt ?: Instant.MIN } ?: Instant.MIN
            )

            val scopeConfig = config?.variables
                ?.map { (id, value) -> SingleVariable(id.asString(), value.asOptString()) }
                ?: emptyList()

            val defaultConfig = buildMap {
                for (variableSet in defaults.asReversed()) {
                    for ((id, value) in variableSet.variables) {
                        val name = id.asString()
                        put(name, SingleVariable(name, value.asOptString(), variableSet.scope.asString()))
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

