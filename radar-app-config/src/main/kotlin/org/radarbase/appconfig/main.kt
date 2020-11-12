package org.radarbase.appconfig

import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.config.ConfigLoader

fun main(args: Array<String>) {
    val config = ConfigLoader.loadConfig(
        listOf("appconfig.yml", "/etc/radar-app-config/appconfig.yml"),
        args,
        ApplicationConfig::class.java
    )
    val resources = ConfigLoader.loadResources(config.inject.enhancerFactory, config)

    val server = GrizzlyServer(config.baseUri, resources, config.isJmxEnabled)
    server.listen()
}
