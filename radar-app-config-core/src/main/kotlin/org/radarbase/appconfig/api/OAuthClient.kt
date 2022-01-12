package org.radarbase.appconfig.api

import com.fasterxml.jackson.annotation.JsonProperty

data class OAuthClient(@JsonProperty("clientId") val id: String)
