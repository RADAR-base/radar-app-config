import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("org.jetbrains.kotlin.jvm").version("1.3.61").apply(false)
    idea
    id("org.jetbrains.gradle.plugin.idea-ext").version("0.7")
}

subprojects {
    repositories {
        // Use jcenter for resolving your dependencies.
        // You can declare any Maven/Ivy/file repository here.
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

tasks.wrapper {
    gradleVersion = "6.1.1"
}
