package org.radarbase.appconfig.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.radarbase.appconfig.client.api.AppConfigConfig

class AppConfigClient(
    baseUrl: String,
    private val authClient: AuthClient,
    configure: (HttpClientConfig<JavaHttpConfig>.() -> Unit)? = null,
) {
    private val baseUrl: Url = Url(baseUrl)

    private val client = HttpClient(Java) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(Auth) {
            authClient.configure(this)
        }
        if (configure != null) configure()
    }

    suspend fun fetchConfig(scope: Scope): AppConfigConfig {
        val response: HttpResponse = client.get(scope.toUrl())
        return response.processAppConfig()
    }

    suspend fun updateConfig(config: AppConfigConfig, scope: Scope): AppConfigConfig {
        val response: HttpResponse = client.post(scope.toUrl()) {
            body = config
        }
        return response.processAppConfig()
    }

    private fun Scope.toUrl(): Url = URLBuilder(baseUrl).run {
        when (this@toUrl) {
            GlobalScope -> pathComponents("global")
            is ConditionScope -> pathComponents("project", projectId, "conditions", conditionId)
            is UserScope -> pathComponents("projects", projectId, "users", userId)
            is ProjectScope -> pathComponents("projects", projectId)
        }
        pathComponents("config", authClient.clientId)
        build()
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

sealed interface Scope

object GlobalScope : Scope {
    override fun toString(): String = "\$g"
}
class UserScope(val projectId: String, val userId: String) : Scope {
    override fun toString(): String = "\$u.$userId.\$p.$projectId"
}
class ConditionScope(val projectId: String, val conditionId: String) : Scope {
    override fun toString(): String = "\$c.$conditionId.\$p.$projectId"
}
class ProjectScope(val projectId: String) : Scope {
    override fun toString(): String = "\$p.$projectId"
}
