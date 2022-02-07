package org.radarbase.appconfig.api

data class User(
    val id: String,
    val externalUserId: String? = null,
    val hasConfig: Boolean? = null,
)
