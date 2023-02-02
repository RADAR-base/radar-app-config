package org.radarbase.lang.expression

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class ExpressionSerializer : StdSerializer<Expression>(Expression::class.java) {
    override fun serialize(value: Expression, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.toString())
    }
}
