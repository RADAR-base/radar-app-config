package org.radarbase.appconfig.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class Project(
    @JsonProperty("projectName") val name: String,
    @JsonProperty("humanReadableProjectName") val humanReadableName: String? = null,
    val location: String? = null,
    val organization: String? = null,
    val description: String? = null
)
