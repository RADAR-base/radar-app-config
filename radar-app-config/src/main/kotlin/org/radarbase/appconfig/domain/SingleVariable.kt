package org.radarbase.appconfig.domain

import com.fasterxml.jackson.annotation.JsonInclude

data class SingleVariable(
    val name: String,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val value: String?,
    val scope: String? = null,
)
