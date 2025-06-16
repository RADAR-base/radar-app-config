plugins {
    id("publishing-convention")
}

description = "Kotlin Client SDK to the radar-app-config API"

dependencies {
    api(project(":radar-app-config-core"))

    // Provided by libs.radar.commons.kotlin
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom"))
    api(platform("io.ktor:ktor-bom"))
    api("io.ktor:ktor-client-core")
    api("io.ktor:ktor-client-auth")

    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    implementation(libs.radar.commons.kotlin)
}
