plugins {
    application
    kotlin("jvm")
}

application {
    mainClassName = "org.radarbase.appconfig.MainKt"
}

project.extra.apply {
    set("radarMpVersion", "0.5.4")
    set("radarCommonsVersion", "0.11.3")
    set("radarSchemasVersion", "0.4.3")
    set("jacksonVersion", "2.9.9")
    set("slf4jVersion", "1.7.25")
    set("logbackVersion", "1.2.3")
    set("grizzlyVersion", "2.4.4")
    set("jerseyVersion", "2.28")
    set("hibernateVersion", "5.4.2.Final")
}

repositories {
    jcenter()
    maven(url = "https://dl.bintray.com/radar-cns/org.radarcns")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    implementation("org.glassfish.grizzly:grizzly-http-server:${project.extra["grizzlyVersion"]}")

    implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:${project.extra["jerseyVersion"]}")
    implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:${project.extra["jerseyVersion"]}")
    implementation("org.glassfish.jersey.inject:jersey-hk2:${project.extra["jerseyVersion"]}")
    runtimeOnly("org.glassfish.jersey.media:jersey-media-json-jackson:${project.extra["jerseyVersion"]}")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${project.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${project.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${project.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${project.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${project.extra["jacksonVersion"]}")

    implementation("org.radarcns:radar-auth:${project.extra["radarMpVersion"]}")

    implementation("org.slf4j:slf4j-api:${project.extra["slf4jVersion"]}")

    implementation("org.hibernate:hibernate-core:${project.extra["hibernateVersion"]}")
    implementation("org.liquibase:liquibase-core:3.5.3")

    runtimeOnly("com.h2database:h2:1.4.199")
    runtimeOnly("org.postgresql:postgresql:42.2.5")
    runtimeOnly("ch.qos.logback:logback-classic:${project.extra["logbackVersion"]}")


//    testImplementation("com.h2database:h2:1.4.199")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register("downloadDependencies") {

    configurations["runtimeClasspath"].files
    configurations["compileClasspath"].files

    doLast {
        println("Downloaded all dependencies")
    }
}
