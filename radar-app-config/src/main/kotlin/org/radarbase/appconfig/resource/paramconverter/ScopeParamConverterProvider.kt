package org.radarbase.appconfig.resource.paramconverter

import jakarta.ws.rs.ext.ParamConverter
import jakarta.ws.rs.ext.ParamConverterProvider
import jakarta.ws.rs.ext.Provider
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.SimpleScope
import java.lang.reflect.Type

@Provider
class ScopeParamConverterProvider : ParamConverterProvider {
    override fun <T : Any?> getConverter(
        rawType: Class<T>?,
        genericType: Type?,
        annotations: Array<out Annotation>?,
    ): ParamConverter<T>? {
        if (rawType == Scope::class.java) {
            return ScopeParamConverter() as ParamConverter<T>
        }
        return null
    }

    class ScopeParamConverter : ParamConverter<Scope> {
        override fun fromString(value: String?): Scope? {
            return value?.let { SimpleScope(it) }
        }

        override fun toString(value: Scope?): String? {
            return value?.asString()
        }
    }
}
