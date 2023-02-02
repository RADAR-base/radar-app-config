package org.radarbase.appconfig.client.config

import java.time.Duration

data class AppConfigServiceConfig(
    val notFoundValidity: Duration = Duration.ofMinutes(15),
    val errorValidity: Duration = Duration.ofSeconds(30),
    val successValidity: Duration = Duration.ofMinutes(10),
)
