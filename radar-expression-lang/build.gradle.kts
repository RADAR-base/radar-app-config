description = "RADAR app condition expression language"

val generateGrammarSource by project(":radar-expression-lang-antlr").tasks

sourceSets {
    main {
        java.srcDir(generateGrammarSource.outputs)
    }
}

dependencies {
    implementation("org.antlr:antlr4-runtime:${Versions.antlr}")

    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:${Versions.coroutines}"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")
}
