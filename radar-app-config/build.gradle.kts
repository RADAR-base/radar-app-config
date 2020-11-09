plugins {
    application
    kotlin("jvm")
}

application {
    mainClassName = "org.radarbase.appconfig.MainKt"
}

repositories {
    jcenter()
    maven(url = "https://dl.bintray.com/radar-base/org.radarbase")
    maven(url = "https://dl.bintray.com/radar-cns/org.radarcns")
    maven(url = "https://repo.thehyve.nl/content/repositories/snapshots")
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

    runtimeOnly("com.h2database:h2:${project.property("h2Version")}")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.2")
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

tasks.register("downloadDependencies") {
    doLast {
        configurations["runtimeClasspath"].files
        configurations["compileClasspath"].files
        println("Downloaded all dependencies")
    }
}
