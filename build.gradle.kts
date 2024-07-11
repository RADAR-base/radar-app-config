import org.radarbase.gradle.plugin.radarKotlin
import org.radarbase.gradle.plugin.radarPublishing

plugins {
    id("org.radarbase.radar-root-project") version Versions.radarCommons
    id("org.radarbase.radar-dependency-management") version Versions.radarCommons
    id("org.radarbase.radar-kotlin") version Versions.radarCommons apply false
    id("org.radarbase.radar-publishing") version Versions.radarCommons apply false
    kotlin("plugin.serialization") version Versions.kotlin apply false
}

radarRootProject {
    projectVersion.set(Versions.project)
}

configure(listOf(
    project(":radar-expression-lang"),
    project(":radar-app-config"),
    project(":radar-app-config-core"),
    project(":radar-app-config-client"),
    project(":radar-expression-lang"),
)) {
    apply(plugin = "org.radarbase.radar-kotlin")

    radarKotlin {
        javaVersion.set(Versions.java)
        kotlinVersion.set(Versions.kotlin)
        slf4jVersion.set(Versions.slf4j)
        log4j2Version.set(Versions.log4j2)
        junitVersion.set(Versions.junit)
    }
}

project(":radar-expression-lang-antlr") {
    apply(plugin = "java")
    apply(plugin = "org.gradle.antlr")

    repositories {
        mavenCentral()
        mavenLocal()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }

    tasks.withType<JavaCompile> {
        options.release.set(11)
    }
}

configure(listOf(
    project(":radar-expression-lang"),
    project(":radar-app-config-core"),
    project(":radar-app-config-client"),
)) {
    apply(plugin = "org.radarbase.radar-publishing")

    radarKotlin {
        javaVersion.set(17)
    }

    radarPublishing {
        val githubRepoName = "RADAR-base/radar-app-config"
        githubUrl.set("https://github.com/$githubRepoName.git")

        developers {
            developer {
                id.set("blootsvoets")
                name.set("Bastiaan de Graaf")
                email.set("bastiaan@thehyve.nl")
                organization.set("The Hyve")
            }
            developer {
                id.set("nivemaham")
                name.set("Nivethika Mahasivam")
                email.set("nivethika@thehyve.nl")
                organization.set("The Hyve")
            }
        }
    }
}
