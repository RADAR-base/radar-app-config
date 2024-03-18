plugins {
    kotlin("plugin.serialization")
}

description = "RADAR app config core API"

dependencies {
    api(project(":radar-expression-lang"))

    api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerialization}")
}
