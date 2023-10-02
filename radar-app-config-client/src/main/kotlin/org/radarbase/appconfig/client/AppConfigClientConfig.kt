package org.radarbase.appconfig.client

import io.ktor.client.HttpClient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * App config client configuration. The type to serialize with can be constructed in Kotlin as
 * `object : TypeReference<MyType>() {}`. It can be any type as long as a Jackson Object Mapper can serialize and
 * deserialize it from a JSON object with contents `{"key1": "value1", ...}`.
 *
 * @param type configuration type to serialize and deserialize from.
 */
@Suppress("unused")
class AppConfigClientConfig {
    /** App-config service URL. */
    var appConfigUrl: String? = null

    /** OAuth 2.0 token endpoint URL. */
    var tokenUrl: String? = null

    /** OAuth 2.0 client ID. */
    var clientId: String? = null

    /** OAuth 2.0 client secret. */
    var clientSecret: String? = null

    /** Http client. */
    var httpClient: HttpClient? = null

    /** Only get configuration options with the given prefix, and strip that prefix before serialization. */
    var configPrefix: String = ""

    /** Time to cache each participant's data after requesting. */
    var cacheMaxAge: Duration = 1.hours

    override fun toString(): String = "AppConfigClientConfig(" +
        "appConfigUrl=$appConfigUrl, " +
        "tokenUrl=$tokenUrl, " +
        "clientId=$clientId, " +
        "clientSecret=$clientSecret, " +
        "configPrefix=$configPrefix, " +
        "cacheMaxAge=$cacheMaxAge)"
}
