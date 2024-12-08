plugins {
    id("publishing-convention")
    alias(libs.plugins.kotlin.serialization)
}

description = "RADAR app config core API"

dependencies {
    api(project(":radar-expression-lang"))
    api(libs.kotlinx.serialization.json)
}
