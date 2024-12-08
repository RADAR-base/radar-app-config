import org.radarbase.gradle.plugin.radarKotlin

plugins {
    id("org.radarbase.radar-kotlin")
}

radarKotlin {
    javaVersion.set(17)
    log4j2Version.set("2.20.0")
}
