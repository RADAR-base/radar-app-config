plugins {
    kotlin("jvm")
    idea
    id("org.jetbrains.gradle.plugin.idea-ext")
}

description = "RADAR app condition expression language"

sourceSets {
    main {
        java.srcDir("$buildDir/generated-src/antlr/main")
    }
}

dependencies {
    val antlrVersion: String by project
    compileOnly(project(":radar-expression-lang-antlr"))
    implementation("org.antlr:antlr4-runtime:$antlrVersion")

    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    val jacksonVersion: String by project
    api(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    api("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    val coroutinesVersion: String by project
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    val mpVersion: String by project
    implementation("org.radarbase:radar-kotlin:$mpVersion")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

idea {
    module {
        generatedSourceDirs = generatedSourceDirs + file("$buildDir/generated-src/antlr/main")
    }
}
