package org.radarbase.appconfig.util

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.util.BufferRecyclers
import com.fasterxml.jackson.databind.ObjectMapper
import org.glassfish.jersey.message.internal.ReaderWriter.UTF8
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

object Json {
    val factory = JsonFactory()
    val mapper = ObjectMapper(factory)
}
