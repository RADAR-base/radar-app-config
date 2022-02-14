package org.radarbase.appconfig.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import java.util.*

class ObjectMapperCache(val mapper: ObjectMapper) {
    private val readerCache = IdentityHashMap<Any, ObjectReader>()
    private val writerCache = IdentityHashMap<Any, ObjectWriter>()

    fun readerFor(type: Class<*>): ObjectReader =
        readerCache.computeIfAbsent(type) { mapper.readerFor(type) }

    fun readerFor(type: TypeReference<*>): ObjectReader =
        readerCache.computeIfAbsent(type) { mapper.readerFor(type) }

    fun writerFor(type: Class<*>): ObjectWriter =
        writerCache.computeIfAbsent(type) { mapper.writerFor(type) }

    fun writerFor(type: TypeReference<*>): ObjectWriter =
        writerCache.computeIfAbsent(type) { mapper.writerFor(type) }
}
