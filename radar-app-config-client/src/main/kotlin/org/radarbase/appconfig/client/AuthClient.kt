package org.radarbase.appconfig.client

import io.ktor.client.plugins.auth.*

interface AuthClient {
    val clientId: String
    fun configure(auth: Auth)
}
