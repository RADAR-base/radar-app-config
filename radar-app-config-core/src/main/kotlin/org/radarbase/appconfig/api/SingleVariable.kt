package org.radarbase.appconfig.api

import kotlinx.serialization.Serializable

@Serializable
data class SingleVariable(
    val name: String,
    val value: String?,
    val scope: String? = null,
    val clientId: String? = null,
    val version: Int? = null,
    val createdByUser: String? = null,
    val createTimestamp: Long? = null,
)
