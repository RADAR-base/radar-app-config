plugins {
    application
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

    implementation(platform("com.fasterxml.jackson:jackson-bom:${Versions.jackson}"))

    implementation("org.radarbase:radar-jersey:${Versions.radarJersey}")
    implementation("org.radarbase:radar-jersey-hibernate:${Versions.radarJersey}")

    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    implementation("com.hazelcast:hazelcast-hibernate53:${Versions.hazelcastHibernate}")
    implementation("com.hazelcast:hazelcast:${Versions.hazelcast}")
    runtimeOnly("com.hazelcast:hazelcast-kubernetes:${Versions.hazelcastKubernetes}")

    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.junit}")
    testImplementation("org.hamcrest:hamcrest:${Versions.hamcrest}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")
}
