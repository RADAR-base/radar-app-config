plugins {
    application
    kotlin("jvm")
}

application {
    mainClassName = "org.radarbase.appconfig.MainKt"
}

project.extra.apply {
    set("okhttpVersion", "4.3.1")
    set("radarAuthVersion", "0.2.3-SNAPSHOT")
    set("radarSchemasVersion", "0.5.5")
    set("jacksonVersion", "2.10.2")
    set("slf4jVersion", "1.7.27")
    set("logbackVersion", "1.2.3")
    set("hibernateVersion", "5.4.4.Final")
    set("h2Version", "1.4.200")
    set("postgresqlVersion", "42.2.9")
    set("liquibaseVersion", "3.8.5")
    set("c3p0Version", "0.9.5.5")
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

    implementation("com.squareup.okhttp3:okhttp:${project.extra["okhttpVersion"]}")
    implementation(project(":radar-expression-lang"))

    implementation("org.radarbase:radar-jersey:${project.extra["radarAuthVersion"]}")

    implementation("com.fasterxml.jackson.core:jackson-databind:${project.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${project.extra["jacksonVersion"]}")

    implementation("org.slf4j:slf4j-api:${project.extra["slf4jVersion"]}")

    implementation("org.hibernate:hibernate-core:${project.extra["hibernateVersion"]}")
    implementation("org.liquibase:liquibase-core:${project.extra["liquibaseVersion"]}")

    runtimeOnly("com.h2database:h2:${project.extra["h2Version"]}")
    runtimeOnly("org.postgresql:postgresql:${project.extra["postgresqlVersion"]}")
    runtimeOnly("ch.qos.logback:logback-classic:${project.extra["logbackVersion"]}")
    runtimeOnly("org.hibernate:hibernate-c3p0:${project.extra["hibernateVersion"]}")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
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
    configurations["runtimeClasspath"].files
    configurations["compileClasspath"].files

    doLast {
        println("Downloaded all dependencies")
    }
}
