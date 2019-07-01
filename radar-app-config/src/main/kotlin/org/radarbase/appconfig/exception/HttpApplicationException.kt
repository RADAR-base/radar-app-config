package org.radarbase.appconfig.exception

import java.lang.RuntimeException
import javax.ws.rs.core.Response

open class HttpApplicationException(val status: Int, val code: String, val detailedMessage: String?) : RuntimeException("[$status] $code: $detailedMessage") {
    constructor(status: Response.Status, code: String, detailedMessage: String?) : this(status.statusCode, code, detailedMessage)
}
