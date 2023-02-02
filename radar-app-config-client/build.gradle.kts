plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

description = "Kotlin Client SDK to the radar-app-config API"

dependencies {
    val ktorVersion: String by project
    implementation(platform("io.ktor:ktor-bom:$ktorVersion"))
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-java")
    implementation("io.ktor:ktor-client-auth")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
}
