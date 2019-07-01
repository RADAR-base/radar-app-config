package org.radarbase.appconfig.exception

import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/** Handle WebApplicationException. This uses the status code embedded in the exception. */
@Provider
@Singleton
class WebApplicationExceptionMapper : ExceptionMapper<WebApplicationException> {
    @Context
    private lateinit var uriInfo: UriInfo

    override fun toResponse(exception: WebApplicationException): Response {
        val response = exception.response
        logger.error("[{}] {}: {}", response.status, uriInfo.absolutePath, exception.message)
        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WebApplicationExceptionMapper::class.java)
    }
}
