package org.radarbase.appconfig.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.kotlin.coroutines.CacheConfig
import org.radarbase.kotlin.coroutines.CachedValue
import org.radarbase.ktor.auth.ClientCredentialsConfig
import org.radarbase.ktor.auth.clientCredentials
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * App config client. The type to serialize with can be constructed in Kotlin as
 * `object : TypeReference<MyType>() {}`. It can be any type as long as a Jackson Object Mapper can serialize and
 * deserialize it from a JSON object with contents `{"key1": "value1", ...}`.
 *
 * @param config configuration to use.
 */
@Suppress("unused")
class AppConfigClient(config: AppConfigClientConfig) {
    private val oauth2ClientId: String
    private val client: HttpClient

    private val baseUrl: Url
    private val configPrefix: String = config.configPrefix

    private val cacheConfig = CacheConfig(refreshDuration = config.cacheMaxAge)
    private val cache: ConcurrentMap<String, CachedValue<ClientConfig>> = ConcurrentHashMap()

    init {
        baseUrl = Url(
            requireNotNull(config.appConfigUrl) { "App config client URL missing in $config" }
                .trimEnd('/') + '/',
        )

        oauth2ClientId = requireNotNull(config.clientId) { "App config client ID missing in $config" }
        client = (config.httpClient ?: HttpClient(CIO)).config {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }

            install(Auth) {
                clientCredentials(
                    authConfig = ClientCredentialsConfig(
                        tokenUrl = requireNotNull(config.tokenUrl) { "App config client token URL missing in $config" },
                        clientId = oauth2ClientId,
                        clientSecret = config.clientSecret,
                    ),
                    targetHost = baseUrl.host,
                )
            }

            defaultRequest {
                url.takeFrom(baseUrl)
            }
        }
    }

    /**
     * App config client. Configure it inside a code block.
     */
    constructor(
        builder: AppConfigClientConfig.() -> Unit,
    ) : this(AppConfigClientConfig().apply(builder))

    /**
     * Get the config of given user. This will respond with a cached value if the cache is valid.
     *
     * @param projectId the project of the user.
     * @param userId the ID of the user
     * @param clientId the OAuth client ID that config should be fetched for. Defaults to the current client ID.
     */
    @Throws(IOException::class)
    suspend fun getUserConfig(projectId: String, userId: String, clientId: String = oauth2ClientId): ClientConfig = userCache(projectId, userId, clientId).get()

    private suspend fun userCache(projectId: String, userId: String, clientId: String = oauth2ClientId) = cache.computeIfAbsent("$projectId:$userId:$clientId") {
        CachedValue(cacheConfig) {
            fetchConfig(projectId, userId, clientId)
        }
    }

    /**
     * Set the config of given user. This will not remove any values on the service but only update ones provided by
     * the config.
     *
     * @param projectId the project of the user.
     * @param userId the ID of the user
     * @param config new configuration values.
     * @param includeKeys if given, only update that subset of keys provided in the config.
     * @param clientId the OAuth client ID that config should be fetched for. Defaults to the current client ID.
     */
    @Throws(IOException::class)
    suspend fun setUserConfig(
        projectId: String,
        userId: String,
        config: ClientConfig,
        includeKeys: Set<String>? = null,
        clientId: String = oauth2ClientId,
    ): ClientConfig {
        var clientConfig = config
        if (includeKeys != null) {
            clientConfig = clientConfig.copy(config = clientConfig.config.filter { it.name in includeKeys })
        }
        val newConfig: ClientConfig = fetchConfig(projectId, userId, clientId)
            .with(clientConfig)

        return putConfig(projectId, userId, clientId, newConfig)
            .also { userCache(projectId, userId, clientId).set(it) }
    }

    @Throws(IOException::class)
    private suspend fun fetchConfig(
        projectId: String,
        userId: String,
        clientId: String,
    ): ClientConfig {
        val request: HttpStatement = buildConfigRequest(projectId, userId, clientId)
        return executeConfigRequest(request)
    }

    @Throws(IOException::class)
    private suspend fun putConfig(
        projectId: String,
        userId: String,
        clientId: String,
        config: ClientConfig,
    ): ClientConfig {
        val request: HttpStatement = buildConfigRequest(projectId, userId, clientId) {
            method = HttpMethod.Post
            setBody(config)
            contentType(ContentType.Application.Json)
        }
        return executeConfigRequest(request)
    }

    @Throws(IOException::class)
    private suspend fun executeConfigRequest(request: HttpStatement): ClientConfig = withContext(Dispatchers.IO) {
        val response = request.execute()
        if (!response.status.isSuccess()) {
            val responseString = try {
                response.bodyAsText().take(256)
            } catch (ex: Throwable) {
                // ignore
            }
            throw IOException("Unknown response from AppConfig: $responseString")
        }
        response.body()
    }

    private suspend fun buildConfigRequest(
        projectId: String,
        userId: String,
        clientId: String,
        builder: (HttpRequestBuilder.() -> Unit) = {},
    ): HttpStatement = client.prepareRequest {
        url {
            appendEncodedPathSegments("projects")
            appendPathSegments(projectId)
            appendEncodedPathSegments("users")
            appendPathSegments(userId)
            appendEncodedPathSegments("config")
            appendPathSegments(clientId)
        }
        builder()
    }
}
