plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":radar-app-config-core"))
    val okhttpVersion: String by project
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")

    val jacksonVersion: String by project
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    val mpVersion: String by project
    implementation("org.radarbase:oauth-client-util:$mpVersion")
}
