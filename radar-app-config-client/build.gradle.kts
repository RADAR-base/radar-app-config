description = "Kotlin Client SDK to the radar-app-config API"

dependencies {
    api(project(":radar-app-config-core"))

    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:${Versions.coroutines}"))
    api(platform("io.ktor:ktor-bom:${Versions.ktor}"))

    api("io.ktor:ktor-client-core")
    api("io.ktor:ktor-client-auth")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")
}
