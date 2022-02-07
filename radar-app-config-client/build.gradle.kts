plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":radar-app-config-core"))
    val okhttpVersion: String by project
    api("com.squareup.okhttp3:okhttp:$okhttpVersion")

    val jacksonVersion: String by project
    api(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    api("com.fasterxml.jackson.core:jackson-databind")

    val mpVersion: String by project
    api("org.radarbase:oauth-client-util:$mpVersion")
}
