plugins {
    application
    id("kotlin-convention")
}

application {
    mainClass.set("org.radarbase.appconfig.MainKt")
    applicationDefaultJvmArgs = listOf(
        "-Djava.security.egd=file:/dev/./urandom",
        "-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager",
        "--add-modules", "java.se",
        "--add-exports", "java.base/jdk.internal.ref=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.nio=ALL-UNNAMED",
        "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens", "java.management/sun.management=ALL-UNNAMED",
        "--add-opens", "jdk.management/com.sun.management.internal=ALL-UNNAMED",
        "-Dhazelcast.security.recommendations",
        "-Dhazelcast.socket.server.bind.any=false",
    )
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation(project(":radar-app-config-core"))

    implementation(platform(libs.jackson.bom))

    implementation(libs.radar.jersey)
    implementation(libs.radar.jersey.hibernate)

    implementation(libs.radar.commons.kotlin)

    implementation(libs.hazelcast)
    implementation(libs.hazelcast.hybernate53)
    implementation(libs.hazelcast.kubernetes)

    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockito.kotlin)
}

radarKotlin {
    sentryEnabled.set(true)
}
