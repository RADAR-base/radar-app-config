plugins {
    id("publishing-convention")
}

description = "RADAR app condition expression language"

val generateGrammarSource by project(":radar-expression-lang-antlr").tasks

sourceSets {
    main {
        java.srcDir(generateGrammarSource.outputs)
    }
}

dependencies {
    implementation(libs.antlr4.runtime)

    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation(libs.radar.commons.kotlin)
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}
