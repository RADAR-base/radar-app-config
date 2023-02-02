package org.radarbase.appconfig.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.*
import org.radarbase.appconfig.client.api.AppConfigConfig

class AppConfigClient(
    baseUrl: String,
    private val authClient: AuthClient,
    configure: (HttpClientConfig<JavaHttpConfig>.() -> Unit)? = null,
) {
    private val baseUrl: Url = Url(baseUrl)

    private val client = HttpClient(Java) {
        install(ContentNegotiation) {
            json()
        }
        install(Auth) {
            authClient.configure(this)
        }
        if (configure != null) configure()
    }

    suspend fun fetchConfig(scope: Scope): AppConfigConfig {
        val response: HttpResponse = client.get(scope.toConfigUrl())
        return response.processAppConfig()
    }

    suspend fun updateConfig(config: AppConfigConfig, scope: Scope): AppConfigConfig {
        val response: HttpResponse = client.post(scope.toConfigUrl()) {
            setBody(config)
            contentType(Json)
        }
        return response.processAppConfig()
    }

    private fun Scope.toConfigUrl(): Url = URLBuilder(baseUrl).run {
        when (this@toConfigUrl) {
            Scope.Global -> appendPathSegments("global")
            is Scope.Condition -> appendPathSegments("project", projectId, "conditions", conditionId)
            is Scope.User -> appendPathSegments("projects", projectId, "users", userId)
            is Scope.Project -> appendPathSegments("projects", projectId)
        }
        appendPathSegments("config", authClient.clientId)
        build()
    }

    companion object {
        suspend fun HttpResponse.processAppConfig(): AppConfigConfig {
            when (status.value) {
                404 -> throw NoSuchElementException()
                409 -> throw ConcurrentModificationException()
                in 400..1000 -> throw IllegalStateException(body<String>())
            }
            return body()
        }
    }
}
