plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":radar-app-config-core"))
    val okhttpVersion: String by project
    api("com.squareup.okhttp3:okhttp:$okhttpVersion")

    val jacksonVersion: String by project
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    val mpVersion: String by project
    api("org.radarbase:oauth-client-util:$mpVersion")
}
