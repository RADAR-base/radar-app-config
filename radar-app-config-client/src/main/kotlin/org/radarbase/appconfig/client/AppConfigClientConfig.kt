package org.radarbase.appconfig.client

import com.fasterxml.jackson.core.type.TypeReference
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.net.MalformedURLException
import java.lang.IllegalArgumentException
import java.net.URL
import java.time.Duration

/**
 * App config client configuration. The type to serialize with can be constructed in Kotlin as
 * `object : TypeReference<MyType>() {}`. It can be any type as long as a Jackson Object Mapper can serialize and
 * deserialize it from a JSON object with contents `{"key1": "value1", ...}`.
 *
 * @param type configuration type to serialize and deserialize from.
 */
@Suppress("unused")
class AppConfigClientConfig<T>(val type: TypeReference<T>) {
    /** App-config service URL. */
    var appConfigUrl: HttpUrl? = null
    /** OAuth 2.0 token endpoint URL. */
    var tokenUrl: URL? = null
    /** OAuth 2.0 client ID. */
    var clientId: String? = null
    /** OAuth 2.0 client secret. */
    var clientSecret: String? = null
    /** Http client. */
    var httpClient: OkHttpClient? = null
        set(value) {
            field = value?.newBuilder()?.build()
        }
    /** JSON object mapper. Should be loaded with the necessary modules and configuration to serialize the config type. */
    var mapper: ObjectMapper? = null
    /** Only get configuration options with the given prefix, and strip that prefix before serialization. */
    var configPrefix: String = ""
    /** Time to cache each participants data after requesting. */
    var cacheMaxAge: Duration = Duration.ofHours(1)
    /** Number of per-participant cache items to keep. */
    var cacheSize = 10_000

    fun tokenUrl(url: String?) {
        tokenUrl = if (url != null) {
            try {
                URL(url)
            } catch (ex: MalformedURLException) {
                throw IllegalArgumentException(ex)
            }
        } else {
            null
        }
    }

    fun appConfigUrl(url: String?) {
        appConfigUrl = url?.toHttpUrl()
    }

    override fun toString(): String = "AppConfigClientConfig(" +
        "type=$type, " +
        "appConfigUrl=$appConfigUrl, " +
        "tokenUrl=$tokenUrl, " +
        "clientId=$clientId, " +
        "clientSecret=$clientSecret, " +
        "configPrefix=$configPrefix, " +
        "cacheMaxAge=$cacheMaxAge, " +
        "cacheSize=$cacheSize)"
}
