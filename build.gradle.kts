import org.radarbase.gradle.plugin.radarKotlin
import org.radarbase.gradle.plugin.radarPublishing

plugins {
    alias(libs.plugins.radar.root.project)
    alias(libs.plugins.radar.dependency.management)
    alias(libs.plugins.radar.publishing) apply false
    alias(libs.plugins.radar.kotlin) apply false
}

radarRootProject {
    projectVersion.set(libs.versions.project)
    gradleVersion.set(libs.versions.gradle)
}

project(":radar-expression-lang-antlr") {
    apply(plugin = "org.gradle.antlr")
}

allprojects {

    repositories {
        mavenCentral()
        mavenLocal()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }

    // --- Vulnerability fixes start ---
    dependencies {
        plugins.withType<JavaPlugin> {
            constraints {
                add("implementation", rootProject.libs.jackson.bom) {
                    because("Force safe version of Jackson across all modules")
                }
                add("implementation", rootProject.libs.jackson.core) {
                    because("Force safe version of Jackson across all modules")
                }
            }
        }
    }
    // --- Vulnerability fixes end ---
}

subprojects {
    apply(plugin = "org.radarbase.radar-kotlin")

    radarKotlin {
        log4j2Version.set(rootProject.libs.versions.log4j2)
    }
}

configure(listOf(
    project(":radar-app-config"),
)) {
    radarKotlin {
        sentryEnabled.set(true)
        openTelemetryAgentEnabled.set(false)
    }
}

configure(
    listOf(
        project(":radar-expression-lang"),
        project(":radar-app-config-core"),
        project(":radar-app-config-client"),
    ),
) {
    apply(plugin = "org.radarbase.radar-publishing")

    radarPublishing {
        val githubRepoName = "RADAR-base/radar-app-config"
        githubUrl.set("https://github.com/$githubRepoName.git")

        developers {
            developer {
                id.set("pvannierop")
                name.set("Pim van Nierop")
                email.set("pim@thehyve.nl")
                organization.set("The Hyve")
            }
        }
    }
}
