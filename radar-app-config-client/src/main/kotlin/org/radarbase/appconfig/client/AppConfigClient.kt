package org.radarbase.appconfig.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.radarbase.appconfig.client.api.AppConfigConfig

class AppConfigClient(
    private val baseUrl: String,
    private val authClient: AuthClient,
    configure: (HttpClientConfig<JavaHttpConfig>.() -> Unit)? = null,
) {
    private val client = HttpClient(Java) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(Auth) {
            authClient.configure(this)
        }
        if (configure != null) configure()
    }

    suspend fun fetchConfig(projectId: String? = null, userId: String? = null): AppConfigConfig {
        val response: HttpResponse = client.get(configUrl(projectId, userId))
        return response.processAppConfig()
    }

    suspend fun updateConfig(config: AppConfigConfig, projectId: String? = null, userId: String? = null): AppConfigConfig {
        val response: HttpResponse = client.post(configUrl(projectId, userId)) {
            body = config
        }
        return response.processAppConfig()
    }

    private fun configUrl(projectId: String?, userId: String?): String {
        val refUrl = when {
            projectId == null -> "global"
            userId == null -> "projects/$projectId"
            else -> "projects/$projectId/users/$userId"
        }
        return "$baseUrl/$refUrl/config/${authClient.clientId}"
    }

    companion object {
        suspend fun HttpResponse.processAppConfig(): AppConfigConfig {
            when (status.value) {
                404 -> throw NoSuchElementException()
                409 -> throw ConcurrentModificationException()
                in 400..1000 -> throw IllegalStateException(receive<String>())
            }
            return receive()
        }
    }
}
