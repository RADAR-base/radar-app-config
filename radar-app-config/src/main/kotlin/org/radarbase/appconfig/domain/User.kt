package org.radarbase.appconfig.domain

data class User(
    val id: String,
    val externalUserId: String? = null,
    val hasConfig: Boolean? = null,
)
