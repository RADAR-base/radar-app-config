plugins {
    application
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.noarg")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("org.jetbrains.kotlin.plugin.allopen")
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

    val jacksonVersion: String by project
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    val liquibaseVersion: String by project
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")
    val hazelcastHibernateVersion: String by project
    implementation("com.hazelcast:hazelcast-hibernate53:$hazelcastHibernateVersion")
    val hazelcastVersion: String by project
    implementation("com.hazelcast:hazelcast:$hazelcastVersion")
    val hazelcastKubernetesVersion: String by project
    runtimeOnly("com.hazelcast:hazelcast-kubernetes:$hazelcastKubernetesVersion")

    implementation("commons-codec:commons-codec:${project.property("commonsCodecVersion")}")
    runtimeOnly("com.h2database:h2:${project.property("h2Version")}")

    val jsonSchemaVersion: String by project
    implementation("com.networknt:json-schema-validator:$jsonSchemaVersion")

    val slf4jVersion: String by project
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
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

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
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
        configurations.compileClasspath.get().files
        println("Downloaded compile-time dependencies")
    }
}

tasks.register<Copy>("copyDependencies") {
    from(configurations.runtimeClasspath.map { it.files })
    into("$buildDir/third-party/")
    doLast {
        println("Copied third-party runtime dependencies")
    }
}
