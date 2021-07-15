package org.radarbase.appconfig.domain

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant

data class ClientProtocol(
    val id: Long? = null,
    val clientId: String,
    val scope: String? = null,
    val contents: JsonNode,
    val lastModifiedAt: Instant? = null,
)
