package org.radarbase.appconfig.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.radarbase.oauth.OAuth2Client
import kotlin.Throws
import org.radarbase.exception.TokenException
import java.io.IOException
import com.fasterxml.jackson.databind.node.ObjectNode
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.SingleVariable
import java.time.Duration

@Suppress("unused")
class AppConfigClient<T>(config: AppConfigClientConfig<T>) {
    private val client: OkHttpClient = config.httpClient ?: OkHttpClient()

    private val oauth2ClientId: String = requireNotNull(config.clientId)
    private val oauth2Client: OAuth2Client = OAuth2Client.Builder()
        .httpClient(client)
        .endpoint(requireNotNull(config.tokenUrl))
        .credentials(oauth2ClientId, requireNotNull(config.clientSecret))
        .build()

    private val baseUrl: HttpUrl = requireNotNull(config.appConfigUrl)
    private val configPrefix: String? = config.configPrefix
    private val type: TypeReference<T> = config.type
    private val cache: LruCache<String, T> = LruCache(Duration.ofHours(1), config.cacheSize)
    private val objectMappers = ObjectMapperCache(config.mapper ?: ObjectMapper())

    constructor(type: TypeReference<T>, builder: AppConfigClientConfig<T>.() -> Unit)
        : this(AppConfigClientConfig(type).apply(builder))

    @Throws(TokenException::class, IOException::class)
    fun getConfig(projectId: String, userId: String): T? = cache.computeIfAbsent(userId) {
        convertToLocalConfig(fetchConfig(projectId, userId))
    }

    @Throws(JsonProcessingException::class)
    private fun convertToLocalConfig(config: ClientConfig): T {
        val node = objectMappers.mapper.createObjectNode().apply {
            addConfig(config.defaults)
            addConfig(config.config)
        }
        return objectMappers.readerFor(type).readValue(node.toString())
    }

    private fun ObjectNode.addConfig(config: List<SingleVariable>?) {
        if (config != null) {
            if (configPrefix == null) {
                config.forEach { (name, value) -> put(name, value) }
            } else {
                config.asSequence()
                    .filter { it.name.startsWith(configPrefix) }
                    .forEach { (name, value) -> put(name.substring(configPrefix.length), value) }
            }
        }
    }

    @Throws(IOException::class, TokenException::class)
    private fun fetchConfig(projectId: String, userId: String): ClientConfig {
        val request: Request = configRequest(projectId, userId).get().build()
        return makeConfigRequest(request)
    }

    @Throws(TokenException::class, IOException::class)
    fun setConfig(projectId: String, userId: String, config: Map<String, String>): T {
        val newConfig: ClientConfig = fetchConfig(projectId, userId).copyWithConfig(config)
        val updatedConfig = putConfig(projectId, userId, newConfig)
        val newValue = convertToLocalConfig(updatedConfig)
        cache[userId] = newValue
        return newValue
    }

    @Throws(IOException::class, TokenException::class)
    private fun putConfig(projectId: String, userId: String, config: ClientConfig): ClientConfig {
        val request: Request = configRequest(projectId, userId)
            .post(objectMappers.writerFor(ClientConfig::class.java)
                .writeValueAsString(config)
                .toRequestBody(APPLICATION_JSON))
            .build()
        return makeConfigRequest(request)
    }

    @Throws(IOException::class)
    private fun makeConfigRequest(request: Request): ClientConfig {
        client.newCall(request).execute().use { response ->
            response.body.use { body ->
                if (!response.isSuccessful || body == null) {
                    var responseString = body?.string() ?: "null"
                    if (responseString.length > 256) {
                        responseString = responseString.substring(0, 255) + '…'
                    }
                    throw IOException("Unknown response from AppConfig: $responseString")
                }
                return objectMappers.readerFor(ClientConfig::class.java).readValue(body.byteStream())
            }
        }
    }

    @Throws(TokenException::class)
    private fun configRequest(projectId: String, userId: String): Request.Builder {
        val token = oauth2Client.validToken.accessToken
        return Request.Builder()
            .url(
                baseUrl.newBuilder()
                    .addEncodedPathSegment("projects")
                    .addPathSegment(projectId)
                    .addEncodedPathSegment("users")
                    .addPathSegment(userId)
                    .addEncodedPathSegment("config")
                    .addPathSegment(oauth2ClientId)
                    .build()
            )
            .header("Authorization", "Bearer $token")
    }

    companion object {
        private val APPLICATION_JSON: MediaType = "application/json".toMediaType()
    }
}
