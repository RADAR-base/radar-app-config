package org.radarbase.appconfig.config

data class JdbcConfig(
        val driver: String,
        val url: String,
        val user: String? = null,
        val password: String? = null,
        val properties: Map<String, String> = emptyMap())
