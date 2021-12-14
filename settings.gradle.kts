/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/5.4.1/userguide/multi_project_builds.html
 */

rootProject.name = "radar-app-config"
include(":radar-app-config")
include(":radar-expression-lang")
include(":radar-app-config-client")

pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    }
}
