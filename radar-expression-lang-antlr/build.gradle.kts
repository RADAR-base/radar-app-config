plugins {
    java
}

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    antlr(libs.antlr)
}

tasks.generateGrammarSource {
    outputDirectory = file(
        "$rootDir/radar-expression-lang/build/generated-src/antlr/main/org/radarbase/lang/expression/antlr",
    )
    arguments = arguments + listOf(
        "-package", "org.radarbase.lang.expression.antlr"
    )
}

tasks.withType<JavaCompile> {
    options.release.set(11)
}
