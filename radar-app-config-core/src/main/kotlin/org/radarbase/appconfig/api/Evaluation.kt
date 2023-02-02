package org.radarbase.appconfig.api

import com.fasterxml.jackson.annotation.JsonInclude

data class Evaluation(
    val clientId: String?,
    val projectId: String?,
    val userId: String?,
    val condition: Condition,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val evaluation: Any?,
)
