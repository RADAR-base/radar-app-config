plugins {
    application
    kotlin("jvm")
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
        "--add-opens", "jdk.management/com.sun.management.internal=ALL-UNNAMED"
    )
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation(project(":radar-expression-lang"))

    val radarJerseyVersion: String by project
    implementation("org.radarbase:radar-jersey:$radarJerseyVersion")
    implementation("org.radarbase:radar-jersey-hibernate:$radarJerseyVersion")

    val hazelcastHibernateVersion: String by project
    implementation("com.hazelcast:hazelcast-hibernate53:$hazelcastHibernateVersion")
    val hazelcastVersion: String by project
    implementation("com.hazelcast:hazelcast:$hazelcastVersion")
    val hazelcastKubernetesVersion: String by project
    runtimeOnly("com.hazelcast:hazelcast-kubernetes:$hazelcastKubernetesVersion")

    implementation("commons-codec:commons-codec:${project.property("commonsCodecVersion")}")
    runtimeOnly("com.h2database:h2:${project.property("h2Version")}")

    val log4j2Version: String by project
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    runtimeOnly("org.apache.logging.log4j:log4j-api:$log4j2Version")
    runtimeOnly("org.apache.logging.log4j:log4j-jul:$log4j2Version")

    val junitVersion: String by project
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        setExceptionFormat("full")
        showStandardStreams = true
    }
}

tasks.withType<Tar> {
    compression = Compression.GZIP
    archiveExtension.set("tar.gz")
}

tasks.register("downloadDependencies") {
    doLast {
        configurations["runtimeClasspath"].files
        configurations["compileClasspath"].files
        println("Downloaded all dependencies")
    }
}
