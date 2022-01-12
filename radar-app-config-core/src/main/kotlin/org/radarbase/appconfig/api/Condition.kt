package org.radarbase.appconfig.api

import nl.thehyve.lang.expression.Expression


data class Condition(
    val id: Long?,
    val name: String?,
    val title: String? = null,
    val expression: Expression,
    val config: Map<String, Map<String, String>>? = null
)
