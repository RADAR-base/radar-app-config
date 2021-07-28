package org.radarbase.appconfig.domain

import org.radarbase.appconfig.persistence.entity.ConditionEntity
import org.radarbase.lang.expression.Expression
import org.radarbase.lang.expression.ExpressionParser
import java.time.Instant

data class ConditionList(val conditions: List<Condition>)

data class Condition(
    val id: Long?,
    val name: String,
    val title: String? = null,
    val expression: Expression? = null,
    val lastModifiedAt: Instant? = null,
    val rank: Float = 0.0f,
) {
    companion object {
        fun ConditionEntity.toCondition(parser: ExpressionParser) = Condition(
            id = id,
            name = name,
            title = title,
            expression = expression?.let { parser.parse(it) },
            rank = rank,
            lastModifiedAt = lastModifiedAt,
        )
    }
}
