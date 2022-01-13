import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.ide.idea.model.IdeaProject
import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.gradle.ext.TaskTriggersConfig

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm")
    antlr
    idea
    id("org.jetbrains.gradle.plugin.idea-ext")
}

sourceSets {
    main {
        java.srcDir("$buildDir/generated-src/antlr/main")
    }
}

dependencies {
    val antlrVersion: String by project
    antlr("org.antlr:antlr4:$antlrVersion")

    implementation("org.antlr:antlr4-runtime:$antlrVersion")

    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    val jacksonVersion: String by project
    api("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks.generateGrammarSource {
    outputDirectory = file("$buildDir/generated-src/antlr/main/org/radarbase/lang/expression/antlr")
    arguments = arguments + listOf(
        "-package", "org.radarbase.lang.expression.antlr"
    )
}

tasks["compileKotlin"].dependsOn(tasks.generateGrammarSource)
tasks["dokkaJavadoc"].dependsOn(tasks.generateGrammarSource)
tasks["sourcesJar"].dependsOn(tasks.generateGrammarSource)


fun Project.idea(block: IdeaModel.() -> Unit) =
    (this as ExtensionAware).extensions.configure("idea", block)

fun IdeaProject.settings(block: ProjectSettings.() -> Unit) =
    (this@settings as ExtensionAware).extensions.configure(block)

fun ProjectSettings.taskTriggers(block: TaskTriggersConfig.() -> Unit) =
    (this@taskTriggers as ExtensionAware).extensions.configure("taskTriggers", block)


idea {
    module {
        generatedSourceDirs = generatedSourceDirs + file("$buildDir/generated-src/antlr/main")
    }
}

rootProject.idea {
    project {
        settings {
            taskTriggers {
                beforeBuild(tasks["generateGrammarSource"])
                beforeRebuild(tasks["generateGrammarSource"])
                afterSync(tasks["generateGrammarSource"])
            }
        }
    }
}
