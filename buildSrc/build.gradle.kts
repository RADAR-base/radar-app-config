plugins {
    `kotlin-dsl` // Need for 'convention plugins'
}

dependencies {
    // Gradle plugins used in the convention plugin, are declared as dependencies here.
    implementation(libs.gradle.radar.kotlin)
    implementation(libs.gradle.radar.publishing)
    implementation(platform(libs.kotlinx.coroutines.bom))
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}
