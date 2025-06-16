plugins {
    id("org.radarbase.radar-root-project")
}

radarRootProject {
    projectVersion.set(properties["projectVersion"] as String)
}

project(":radar-expression-lang-antlr") {
    apply(plugin = "org.gradle.antlr")
}
