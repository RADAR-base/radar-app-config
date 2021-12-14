package org.radarbase.appconfig.client.api

data class AppConfigField(
    val name: String,
    val value: String?,
    val scope: String?,
)
