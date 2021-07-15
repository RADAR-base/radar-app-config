package org.radarbase.appconfig.domain

import nl.thehyve.lang.expression.QualifiedId
import nl.thehyve.lang.expression.Scope
import nl.thehyve.lang.expression.VariableSet
import nl.thehyve.lang.expression.toVariable
import java.time.Instant

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
