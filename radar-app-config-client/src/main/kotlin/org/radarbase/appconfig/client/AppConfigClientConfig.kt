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

@Suppress("unused")
class AppConfigClientConfig<T>(val type: TypeReference<T>) {
    var appConfigUrl: HttpUrl? = null
    var tokenUrl: URL? = null
    var clientId: String? = null
    var clientSecret: String? = null
    var httpClient: OkHttpClient? = null
        set(value) {
            field = value?.newBuilder()?.build()
        }
    var mapper: ObjectMapper? = null
    var configPrefix: String? = null
    var cacheMaxAge: Duration = Duration.ofHours(1)
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
