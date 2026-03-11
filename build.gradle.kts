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

allprojects {

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
