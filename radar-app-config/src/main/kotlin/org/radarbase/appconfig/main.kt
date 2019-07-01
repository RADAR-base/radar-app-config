package org.radarbase.appconfig

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

val logger: Logger = LoggerFactory.getLogger("org.radarbase.appconfig")

fun loadConfig(args: Array<String>): Config {
    val configFileName = when {
        args.size == 1 -> args[0]
        Files.exists(Paths.get("gateway.yml")) -> "gateway.yml"
        else -> null
    }
    return if (configFileName != null) {
        val configFile = File(configFileName)
        logger.info("Reading configuration from ${configFile.absolutePath}")
        try {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.readValue(configFile, Config::class.java)
        } catch (ex: IOException) {
            logger.error("Usage: radar-gateway [config.yml]")
            logger.error("Failed to read config file $configFile: ${ex.message}")
            exitProcess(1)
        }
    } else Config()
}

fun main(args: Array<String>) {
    val config = loadConfig(args)

    val server = GrizzlyServer(config)

    // register shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread(Runnable {
        logger.info("Stopping server..")
        server.shutdown()
    }, "shutdownHook"))

    try {
        server.start()

        logger.info(String.format("Jersey app started on %s.\nPress Ctrl+C to exit...",
                config.baseUri))
        Thread.currentThread().join()
    } catch (e: Exception) {
        logger.error("Error starting server: $e")
    }
}
