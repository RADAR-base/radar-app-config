package org.radarbase.appconfig.api

import java.time.Instant
import org.radarbase.lang.expression.Expression
import org.radarbase.lang.expression.ExpressionParser

data class Condition(
    val id: Long?,
    val name: String,
    val title: String? = null,
    val expression: Expression? = null,
    val lastModifiedAt: Instant? = null,
    val rank: Float = 0.0f,
) {
    companion object {

    }
}
