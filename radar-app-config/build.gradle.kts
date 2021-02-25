plugins {
    application
    kotlin("jvm")
}

application {
    mainClass.set("org.radarbase.appconfig.MainKt")
    applicationDefaultJvmArgs = listOf(
        "-Djava.security.egd=file:/dev/./urandom",
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

    implementation("com.hazelcast:hazelcast-hibernate53:${project.property("hazelcastHibernateVersion")}")
    implementation("com.hazelcast:hazelcast:${project.property("hazelcastVersion")}")
    runtimeOnly("com.hazelcast:hazelcast-kubernetes:${project.property("hazelcastKubernetesVersion")}")

    implementation("commons-codec:commons-codec:${project.property("commonsCodecVersion")}")
    runtimeOnly("com.h2database:h2:${project.property("h2Version")}")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.1")
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
