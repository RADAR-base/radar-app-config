import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") apply false
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("com.github.ben-manes.versions")
    id("org.jetbrains.dokka") apply false
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

allprojects {
    group = "org.radarbase"
    version = "0.4.3-SNAPSHOT"
}

val githubRepoName = "RADAR-base/radar-app-config"
val githubUrl = "https://github.com/$githubRepoName.git"
val githubIssueUrl = "https://github.com/$githubRepoName/issues"

subprojects {
    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
    tasks.withType<JavaCompile> {
        options.release.set(17)
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            languageVersion = "1.6"
            apiVersion = "1.6"
        }
    }
    afterEvaluate {
        configurations.all {
            resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
            resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
        }
    }
}

project(":radar-expression-lang-antlr") {
    tasks.withType<JavaCompile> {
        options.release.set(11)
    }
}

configure(listOf(
    project(":radar-expression-lang"),
    project(":radar-app-config-core"),
    project(":radar-app-config-client"),
)) {
    val myProject = this
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    dependencies {
        val dokkaVersion: String by project
        val dokkaHtmlPlugin by configurations
        dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:$dokkaVersion")

        val jacksonVersion: String by project
        val dokkaPlugin by configurations
        dokkaPlugin(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
        val dokkaRuntime by configurations
        dokkaRuntime(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))

        val jsoupVersion: String by project
        dokkaPlugin("org.jsoup:jsoup:$jsoupVersion")
        dokkaRuntime("org.jsoup:jsoup:$jsoupVersion")
    }

    tasks.withType<JavaCompile> {
        options.release.set(11)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    val sourcesJar by tasks.registering(Jar::class) {
        from(myProject.the<SourceSetContainer>()["main"].allSource)
        archiveClassifier.set("sources")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        val classes by tasks
        dependsOn(classes)
    }

    val dokkaJar by tasks.registering(Jar::class) {
        from("$buildDir/dokka/javadoc")
        archiveClassifier.set("javadoc")
        val dokkaJavadoc by tasks
        dependsOn(dokkaJavadoc)
    }

    tasks.withType<Tar> {
        compression = Compression.GZIP
        archiveExtension.set("tar.gz")
    }

    tasks.withType<Jar> {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
    }

    val assemble by tasks
    assemble.dependsOn(sourcesJar)
    assemble.dependsOn(dokkaJar)

    val mavenJar by publishing.publications.creating(MavenPublication::class) {
        from(components["java"])

        artifact(sourcesJar)
        artifact(dokkaJar)

        afterEvaluate {
            pom {
                name.set(myProject.name)
                description.set(myProject.description)
                url.set(githubUrl)
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("blootsvoets")
                        name.set("Joris Borgdorff")
                        email.set("joris@thehyve.nl")
                        organization.set("The Hyve")
                    }
                    developer {
                        id.set("nivemaham")
                        name.set("Nivethika Mahasivam")
                        email.set("nivethika@thehyve.nl")
                        organization.set("The Hyve")
                    }
                }
                issueManagement {
                    system.set("GitHub")
                    url.set(githubIssueUrl)
                }
                organization {
                    name.set("RADAR-base")
                    url.set("https://radar-base.org")
                }
                scm {
                    connection.set("scm:git:$githubUrl")
                    url.set(githubUrl)
                }
            }
        }
    }

    signing {
        useGpgCmd()
        isRequired = true
        sign(tasks["sourcesJar"], tasks["dokkaJar"])
        sign(mavenJar)
    }

    tasks.withType<Sign> {
        onlyIf { gradle.taskGraph.hasTask(myProject.tasks["publish"]) }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun Project.propertyOrEnv(propertyName: String, envName: String): String? {
    return if (hasProperty(propertyName)) {
        property(propertyName)?.toString()
    } else {
        System.getenv(envName)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(propertyOrEnv("ossrh.user", "OSSRH_USER"))
            password.set(propertyOrEnv("ossrh.password", "OSSRH_PASSWORD"))
        }
    }
}

tasks.wrapper {
    gradleVersion = "7.6"
}
