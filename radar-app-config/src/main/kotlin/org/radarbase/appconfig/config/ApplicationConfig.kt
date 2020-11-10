package org.radarbase.appconfig.config

import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import java.net.URI

data class ApplicationConfig(
    val baseUri: URI = URI.create("http://0.0.0.0:8090/appconfig/"),
    val isJmxEnabled: Boolean = true,
    val isCorsEnabled: Boolean = false,
    val auth: AuthConfig = AuthConfig(jwtResourceName = "res_appconfig"),
    val inject: InjectConfig = InjectConfig(),
    val database: DatabaseConfig? = null,
    val hazelcast: HazelcastConfig = HazelcastConfig(),
)
