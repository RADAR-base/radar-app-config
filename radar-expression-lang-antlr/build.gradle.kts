dependencies {
    antlr("org.antlr:antlr4:${Versions.antlr}")
}

tasks.generateGrammarSource {
    outputDirectory = file(
        "$rootDir/radar-expression-lang/build/generated-src/antlr/main/org/radarbase/lang/expression/antlr",
    )
    arguments = arguments + listOf(
        "-package", "org.radarbase.lang.expression.antlr"
    )
}
