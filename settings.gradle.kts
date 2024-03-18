rootProject.name = "radar-app-config"

include(":radar-app-config")
include(":radar-expression-lang")
include(":radar-expression-lang-antlr")
include(":radar-app-config-client")
include(":radar-app-config-core")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
