package org.radarbase.appconfig.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class OAuthClient(@JsonProperty("clientId") val id: String)
