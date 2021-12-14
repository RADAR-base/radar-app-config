plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    val ktorVersion: String by project
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-java:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
}
