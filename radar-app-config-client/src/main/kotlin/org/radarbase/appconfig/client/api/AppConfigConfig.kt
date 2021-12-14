package org.radarbase.appconfig.client.api

data class AppConfigConfig(
    val scope: String?,
    val clientId: String?,
    val config: List<AppConfigField>,
    val defaults: List<AppConfigField>?,
) {
    val configMap: Map<String, String> by lazy {
        (defaults?.toMap() ?: emptyMap()) + config.toMap()
    }

    companion object {
        fun List<AppConfigField>.toMap(): Map<String, String> =
            filter { it.value != null }
            .associate { it.name to it.value!! }
    }
}
