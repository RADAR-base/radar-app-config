import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask

description = "RADAR app condition expression language"

sourceSets {
    main {
        java.srcDir("$buildDir/generated-src/antlr/main")
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

val generateGrammarSource by project(":radar-expression-lang-antlr").tasks
tasks.withType<KtLintCheckTask> {
    dependsOn(generateGrammarSource)
}
tasks.withType<KtLintFormatTask> {
    dependsOn(generateGrammarSource)
}
tasks.withType<KotlinCompile> {
    dependsOn(generateGrammarSource)
}
tasks.withType<JavaCompile> {
    dependsOn(generateGrammarSource)
}
tasks.withType<DokkaTask> {
    dependsOn(generateGrammarSource)
}
