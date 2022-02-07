plugins {
    kotlin("jvm")
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

description = "RADAR app config core API"

dependencies {
    val jacksonVersion: String by project
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api(project(":radar-expression-lang"))
}
