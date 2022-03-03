# App config client

Kotlin client library for the radar-app-config service. It uses OkHttp3 for communication and Jackson JSON parsing.
It may cache values.

Import it with:

```gradle
dependencies {
    implementation("org.radarbase:radar-app-config-client:<release version>")
}
```

Example use:

```kotlin
data class MyConfig(val paramA: String?)

val appConfigClient = AppConfigClient(object : TypeReference<MyConfig>() {}) {
    appConfigUrl(appConfigUrl)
    tokenUrl(authConfig.tokenUrl)
    clientId = authConfig.clientId
    clientSecret = authConfig.clientSecret
}

val myConfig = appConfigClient.getUserConfig(projectId, userId)
val newConfig = updateConfig(myConfig)
appConfigClient.setUserConfig(projectId, userId, newConfig)
```
