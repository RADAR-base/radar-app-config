package org.radarbase.appconfig.client

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.radarbase.appconfig.client.api.TokenInfo

@Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
class ClientCredentialsOAuthClient(
    private val tokenUrl: String,
    private val clientCredentials: BasicAuthCredentials,
    configure: (HttpClientConfig<JavaHttpConfig>.() -> Unit)? = null,
): AuthClient {
    override val clientId: String
        get() = clientCredentials.username

    private val tokenClient = HttpClient(Java) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(Auth) {
            basic {
                credentials {
                    clientCredentials
                }
            }
        }
        if (configure != null) configure()
    }

    override fun configure(auth: Auth) {
        auth.bearer {
            lateinit var tokenInfo: TokenInfo
            var refreshTokenInfo: TokenInfo

            loadTokens {
                tokenInfo = tokenClient.submitForm(
                    url = tokenUrl,
                    formParameters = Parameters.build {
                        append("grant_type", "client_credentials")
                        append("client_id", clientCredentials.username)
                        append("client_secret", clientCredentials.password)
                    }
                )
                BearerTokens(
                    accessToken = tokenInfo.accessToken,
                    refreshToken = tokenInfo.refreshToken!!
                )
            }
            refreshTokens {
                refreshTokenInfo = tokenClient.submitForm(
                    url = tokenUrl,
                    formParameters = Parameters.build {
                        append("grant_type", "refresh_token")
                        append("client_id", clientCredentials.username)
                        append("refresh_token", tokenInfo.refreshToken!!)
                    }
                )
                BearerTokens(
                    accessToken = refreshTokenInfo.accessToken,
                    refreshToken = tokenInfo.refreshToken!!
                )
            }
        }
    }
}
