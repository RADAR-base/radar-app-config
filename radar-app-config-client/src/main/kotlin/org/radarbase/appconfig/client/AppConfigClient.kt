package org.radarbase.appconfig.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.exception.TokenException
import org.radarbase.oauth.OAuth2Client
import java.io.IOException

@Suppress("unused")
class AppConfigClient<T>(config: AppConfigClientConfig<T>) {
    private val oauth2ClientId: String
    private val oauth2Client: OAuth2Client

    private val baseUrl: HttpUrl
    private val configPrefix: String? = config.configPrefix
    private val type: TypeReference<T> = config.type

    private val cache: LruCache<String, T> = LruCache(config.cacheMaxAge, config.cacheSize)
    private val objectMappers = ObjectMapperCache(config.mapper ?: ObjectMapper())
    private val client: OkHttpClient = config.httpClient ?: OkHttpClient()
    private val mapReader: ObjectReader by lazy {
        objectMappers.readerFor(object : TypeReference<Map<String, Any>>() {})
    }

    init {
        oauth2ClientId = requireNotNull(config.clientId) { "App config client ID missing in $config" }
        oauth2Client = OAuth2Client.Builder().apply {
            httpClient(client)
            endpoint(requireNotNull(config.tokenUrl) { "App config client token URL missing in $config" })
            credentials(oauth2ClientId, requireNotNull(config.clientSecret) { "App config client secret missing in $config" })
        }.build()
        baseUrl = requireNotNull(config.appConfigUrl) { "App config client URL missing in $config" }
    }

    constructor(
        type: TypeReference<T>,
        builder: AppConfigClientConfig<T>.() -> Unit
    ) : this(AppConfigClientConfig(type).apply(builder))

    @Throws(TokenException::class, IOException::class)
    fun getUserConfig(projectId: String, userId: String, clientId: String = oauth2ClientId): T = cache.computeIfAbsent(userId) {
        fetchConfig(projectId, userId, clientId)
            .convertToLocalConfig()
    }

    @Throws(TokenException::class, IOException::class)
    fun setUserConfig(
        projectId: String,
        userId: String,
        config: T,
        includeKeys: Set<String>? = null,
        clientId: String = oauth2ClientId,
    ): T {
        val stringValue = objectMappers.writerFor(type).writeValueAsString(config)
        var result: Map<String, Any> = mapReader.readValue(stringValue)

        if (includeKeys != null) {
            result = result.filterKeys { it in includeKeys }
        }
        val newConfig: ClientConfig = fetchConfig(projectId, userId, clientId)
            .copyWithConfig(result.mapValues { (_, v) -> v.toString() })

        return putConfig(projectId, userId, clientId, newConfig)
            .convertToLocalConfig()
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
                objectMappers.writerFor(ClientConfig::class.java)
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
                return objectMappers.readerFor(ClientConfig::class.java)
                    .readValue(body.byteStream())
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
    private fun ClientConfig.convertToLocalConfig(): T {
        val node = objectMappers.mapper.createObjectNode().apply {
            val values = (defaults?.asSequence() ?: emptySequence()) + config.asSequence()
            if (configPrefix == null) {
                values.forEach { (name, value) -> put(name, value) }
            } else {
                values
                    .filter { it.name.startsWith(configPrefix) && it.name.length > configPrefix.length }
                    .forEach { (name, value) -> put(name.substring(configPrefix.length), value) }
            }
        }
        return objectMappers.readerFor(type).readValue(node.toString())
    }

    companion object {
        private val APPLICATION_JSON: MediaType = "application/json".toMediaType()
    }
}
