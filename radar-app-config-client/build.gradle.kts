plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

description = "Kotlin Client SDK to the radar-app-config API"

dependencies {
    val ktorVersion: String by project
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-java:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
}
