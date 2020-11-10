import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply false
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("org.jetbrains.gradle.plugin.idea-ext") version "0.9"
}

subprojects {
    version = "0.2.2-SNAPSHOT"

    repositories {
        // Use jcenter for resolving your dependencies.
        // You can declare any Maven/Ivy/file repository here.
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            languageVersion = "1.4"
            apiVersion = "1.4"
        }
    }
}

tasks.wrapper {
    gradleVersion = "6.7"
}
