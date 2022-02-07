import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.ide.idea.model.IdeaProject
import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.gradle.ext.TaskTriggersConfig

plugins {
    idea
    antlr
}

dependencies {
    val antlrVersion: String by project
    antlr("org.antlr:antlr4:$antlrVersion")
}

tasks.generateGrammarSource {
    outputDirectory = file(
        "$rootDir/radar-expression-lang/build/generated-src/antlr/main/org/radarbase/lang/expression/antlr",
    )
    arguments = arguments + listOf(
        "-package", "org.radarbase.lang.expression.antlr"
    )
}

fun Project.idea(block: IdeaModel.() -> Unit) =
    (this as ExtensionAware).extensions.configure("idea", block)

fun IdeaProject.settings(block: ProjectSettings.() -> Unit) =
    (this@settings as ExtensionAware).extensions.configure(block)

fun ProjectSettings.taskTriggers(block: TaskTriggersConfig.() -> Unit) =
    (this@taskTriggers as ExtensionAware).extensions.configure("taskTriggers", block)


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
