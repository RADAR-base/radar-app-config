package org.radarbase.appconfig.api

data class Project(
    val projectName: String,
    val humanReadableProjectName: String? = null,
    val location: String? = null,
    val organization: String? = null,
    val description: String? = null,
)
