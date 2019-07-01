package org.radarbase.appconfig.exception

import javax.ws.rs.core.Response

class RequestEntityTooLarge(message: String): HttpApplicationException(Response.Status.REQUEST_ENTITY_TOO_LARGE, "request_entity_too_large", message)
