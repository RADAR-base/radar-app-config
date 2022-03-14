package org.radarbase.appconfig.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.SingleVariable
import org.radarbase.exception.TokenException
import org.radarbase.oauth.OAuth2Client
import java.io.IOException

/**
 * App config client. The type to serialize with can be constructed in Kotlin as
 * `object : TypeReference<MyType>() {}`. It can be any type as long as a Jackson Object Mapper can serialize and
 * deserialize it from a JSON object with contents `{"key1": "value1", ...}`.
 *
 * @param config configuration to use.
 */
@Suppress("unused")
class AppConfigClient<T>(config: AppConfigClientConfig<T>) {
    private val oauth2ClientId: String
    private val oauth2Client: OAuth2Client

    private val baseUrl: HttpUrl
    private val configPrefix: String = config.configPrefix
    private val type: TypeReference<T> = config.type

    private val cache: LruCache<String, T> = LruCache(config.cacheMaxAge, config.cacheSize)
    private val objectMapper = config.mapper ?: ObjectMapper()
    private val client: OkHttpClient = config.httpClient ?: OkHttpClient()

    private val mapReader by objectMapper.lazyReaderFor(object : TypeReference<Map<String, Any>>() {})
    private val typedConfigReader by objectMapper.lazyReaderFor(config.type)
    private val typedConfigWriter by objectMapper.lazyWriterFor(config.type)
    private val clientConfigWriter by objectMapper.lazyWriterFor(ClientConfig::class.java)
    private val clientConfigReader by objectMapper.lazyReaderFor(ClientConfig::class.java)

    init {
        oauth2ClientId = requireNotNull(config.clientId) { "App config client ID missing in $config" }
        oauth2Client = OAuth2Client.Builder().apply {
            httpClient(client)
            endpoint(requireNotNull(config.tokenUrl) { "App config client token URL missing in $config" })
            credentials(oauth2ClientId, requireNotNull(config.clientSecret) { "App config client secret missing in $config" })
        }.build()
        baseUrl = requireNotNull(config.appConfigUrl) { "App config client URL missing in $config" }
    }

    /**
     * App config client. Configure it inside a code block.
     *
     * @param type deserialization type.
     */
    constructor(
        type: TypeReference<T>,
        builder: AppConfigClientConfig<T>.() -> Unit
    ) : this(AppConfigClientConfig(type).apply(builder))

    /**
     * Get the config of given user. This will respond with a cached value if the cache is valid.
     *
     * @param projectId the project of the user.
     * @param userId the ID of the user
     * @param clientId the OAuth client ID that config should be fetched for. Defaults to the current client ID.
     */
    @Throws(TokenException::class, IOException::class)
    fun getUserConfig(projectId: String, userId: String, clientId: String = oauth2ClientId): T = cache.computeIfAbsent(userId) {
        fetchConfig(projectId, userId, clientId)
            .toTypedConfig()
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
    @Throws(TokenException::class, IOException::class)
    fun setUserConfig(
        projectId: String,
        userId: String,
        config: T,
        includeKeys: Set<String>? = null,
        clientId: String = oauth2ClientId,
    ): T {
        var clientConfig = config.toClientConfig()
        if (includeKeys != null) {
            clientConfig = clientConfig.copy(config = clientConfig.config.filter { it.name in includeKeys })
        }
        val newConfig: ClientConfig = fetchConfig(projectId, userId, clientId)
            .with(clientConfig)

        return putConfig(projectId, userId, clientId, newConfig)
            .toTypedConfig()
            .also { cache[userId] = it }
    }

    @Throws(IOException::class, TokenException::class)
    private fun fetchConfig(
        projectId: String,
        userId: String,
        clientId: String,
    ): ClientConfig {
        val request: Request = buildConfigRequest(projectId, userId, clientId) {
            get()
        }
        return executeConfigRequest(request)
    }

    @Throws(IOException::class, TokenException::class)
    private fun putConfig(
        projectId: String,
        userId: String,
        clientId: String,
        config: ClientConfig
    ): ClientConfig {
        val request: Request = buildConfigRequest(projectId, userId, clientId) {
            post(
                clientConfigWriter
                    .writeValueAsString(config)
                    .toRequestBody(APPLICATION_JSON)
            )
        }
        return executeConfigRequest(request)
    }

    @Throws(IOException::class)
    private fun executeConfigRequest(request: Request): ClientConfig {
        client.newCall(request).execute().use { response ->
            response.body.use { body ->
                if (!response.isSuccessful || body == null) {
                    var responseString = body?.string() ?: "null"
                    if (responseString.length > 256) {
                        responseString = responseString.substring(0, 255) + '…'
                    }
                    throw IOException("Unknown response from AppConfig: $responseString")
                }
                return clientConfigReader.readValue(body.byteStream())
            }
        }
    }

    @Throws(TokenException::class)
    private fun buildConfigRequest(
        projectId: String,
        userId: String,
        clientId: String,
        builder: (Request.Builder.() -> Unit) = {}
    ): Request {
        val token = oauth2Client.validToken.accessToken
        return Request.Builder().apply {
            url(
                baseUrl.newBuilder().apply {
                    addEncodedPathSegment("projects")
                    addPathSegment(projectId)
                    addEncodedPathSegment("users")
                    addPathSegment(userId)
                    addEncodedPathSegment("config")
                    addPathSegment(clientId)
                }.build()
            )
            header("Authorization", "Bearer $token")
            builder()
        }.build()
    }

    @Throws(JsonProcessingException::class)
    private fun ClientConfig.toTypedConfig(): T {
        val node = objectMapper.createObjectNode().apply {
            ((defaults?.asSequence() ?: emptySequence()) + config.asSequence())
                .filter { it.name.length > configPrefix.length && it.name.startsWith(configPrefix) }
                .forEach { (name, value) -> put(name.substring(configPrefix.length), value) }
        }
        return typedConfigReader.readValue(node.toString())
    }

    @Throws(JsonProcessingException::class)
    private fun T.toClientConfig(): ClientConfig {
        val stringValue = typedConfigWriter.writeValueAsString(this)
        val result: Map<String, Any> = mapReader.readValue(stringValue)
        return ClientConfig(
            clientId = null,
            scope = null,
            config = result.map { (k, v) -> SingleVariable(configPrefix + k, v.toString()) },
        )
    }

    companion object {
        private val APPLICATION_JSON: MediaType = "application/json".toMediaType()

        private fun <T> ObjectMapper.lazyWriterFor(type: TypeReference<T>) = lazy { writerFor(type) }
        private fun <T> ObjectMapper.lazyWriterFor(type: Class<T>) = lazy { writerFor(type) }

        private fun <T> ObjectMapper.lazyReaderFor(type: TypeReference<T>) = lazy { readerFor(type) }
        private fun <T> ObjectMapper.lazyReaderFor(type: Class<T>) = lazy { readerFor(type) }
    }
}
