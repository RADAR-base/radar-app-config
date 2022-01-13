plugins {
    kotlin("jvm")
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

dependencies {
    val jacksonVersion: String by project
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api(project(":radar-expression-lang"))
}
