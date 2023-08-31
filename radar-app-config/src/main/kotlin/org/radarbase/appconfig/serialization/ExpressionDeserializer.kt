package org.radarbase.appconfig.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.radarbase.lang.expression.Expression
import org.radarbase.lang.expression.ExpressionParser

class ExpressionDeserializer(
    private val parser: ExpressionParser
) : StdDeserializer<Expression>(Expression::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): Expression {
        val value = jp.codec.readValue(jp, String::class.java)
        return parser.parse(value)
    }
}
