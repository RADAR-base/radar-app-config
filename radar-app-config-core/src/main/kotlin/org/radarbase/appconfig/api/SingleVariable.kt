package org.radarbase.appconfig.api

data class SingleVariable(
    val name: String,
    val value: String?,
    val scope: String? = null,
)
