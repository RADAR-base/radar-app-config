plugins {
    kotlin("jvm")
}

dependencies {
    val jacksonVersion: String by project
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api(project(":radar-expression-lang"))
}
