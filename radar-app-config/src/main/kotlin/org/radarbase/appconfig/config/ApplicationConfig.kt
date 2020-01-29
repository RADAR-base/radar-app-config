package org.radarbase.appconfig.config

import java.net.URI

data class ApplicationConfig(
        val baseUri: URI = URI.create("http://0.0.0.0:8090/appconfig/"),
        val isJmxEnabled: Boolean = true,
        val isCorsEnabled: Boolean = false,
        val authentication: AuthenticationConfig = AuthenticationConfig(),
        val inject: InjectConfig = InjectConfig(),
        val jdbc: JdbcConfig? = null)
