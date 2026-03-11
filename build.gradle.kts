plugins {
    id("org.radarbase.radar-root-project")
}

radarRootProject {
    projectVersion.set(properties["projectVersion"] as String)
    gradleVersion.set(libs.versions.gradle)
}

project(":radar-expression-lang-antlr") {
    apply(plugin = "org.gradle.antlr")
}
