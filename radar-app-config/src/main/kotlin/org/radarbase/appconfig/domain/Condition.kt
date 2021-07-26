package org.radarbase.appconfig.domain

import org.radarbase.lang.expression.Expression

data class ConditionList(val conditions: List<Condition>)

data class Condition(
    val id: Long?,
    val name: String?,
    val title: String? = null,
    val expression: Expression,
    val config: Map<String, Map<String, String>>? = null
)
