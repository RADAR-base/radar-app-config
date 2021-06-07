import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") apply false
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.0"
    id("com.github.ben-manes.versions") version "0.38.0" apply false
}

subprojects {
    version = "0.3.3-SNAPSHOT"

    repositories {
        // Use jcenter for resolving your dependencies.
        // You can declare any Maven/Ivy/file repository here.
        mavenCentral()

        // Temporary until Dokka is fully published on maven central.
        // https://github.com/Kotlin/kotlinx.html/issues/81
        maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")

        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            languageVersion = "1.4"
            apiVersion = "1.4"
        }
    }

    apply(plugin = "com.github.ben-manes.versions")

    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }

    tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }
}

tasks.wrapper {
    gradleVersion = "7.0.2"
}
