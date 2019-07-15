package org.radarbase.appconfig.managementportal

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.radarbase.appconfig.Config
import org.radarbase.appconfig.auth.Auth
import org.radarbase.appconfig.domain.OAuthClient
import org.radarbase.appconfig.domain.Project
import org.radarbase.appconfig.exception.BadGatewayException
import org.radarcns.auth.authorization.Permission
import java.net.MalformedURLException
import java.time.Duration
import java.time.Instant
import javax.ws.rs.core.Context

class MPClient(@Context config: Config, @Context private val auth: Auth) {
    private val clientId: String = config.clientId
    private val clientSecret: String = config.clientSecret ?: throw IllegalArgumentException("Cannot configure managementportal client without client secret")
    private val httpClient = OkHttpClient()
    private val baseUrl: HttpUrl = config.managementPortalUrl.toHttpUrlOrNull()
            ?: throw MalformedURLException("Cannot parse base URL ${config.managementPortalUrl} as an URL")
    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val projectListReader = mapper.readerFor(object : TypeReference<List<Project>>(){})
    private val clientListReader = mapper.readerFor(object : TypeReference<List<OAuthClient>>(){})

    private var token: String? = null
    private var expiration: Instant? = null

    private val validToken: String?
        get() {
            val localToken = token ?: return null
            expiration?.takeIf { it > Instant.now() } ?: return null
            return localToken
        }

    private fun ensureToken(): String {
        var localToken = validToken

        return if (localToken != null) {
            localToken
        } else {
            val result = mapper.readTree(execute(Request.Builder().apply {
                url(baseUrl.resolve("oauth/token")!!)
                post(FormBody.Builder().apply {
                    add("grant_type", "client_credentials")
                    add("client_id", clientId)
                    add("client_secret", clientSecret)
                }.build())
                header("Authorization", Credentials.basic(clientId, clientSecret))
            }.build()))

            localToken = result["access_token"].asText()
                    ?: throw BadGatewayException("ManagementPortal did not provide an access token")
            expiration = Instant.now() + Duration.ofSeconds(result["expires_in"].asLong()) - Duration.ofMinutes(5)
            token = localToken
            localToken
        }
    }

    fun readProjects(): List<Project> {
        val request = Request.Builder().apply {
            url(baseUrl.resolve("api/projects")!!)
            header("Authorization", "Bearer ${ensureToken()}")
        }.build()

        return projectListReader.readValue<List<Project>>(execute(request))
                .filter { auth.hasPermissionOnProject(Permission.PROJECT_READ, it.name) }
    }

    private fun execute(request: Request): String {
        return httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
               response.body?.string()
                       ?: throw BadGatewayException("ManagementPortal did not provide a result")
            } else {
                throw BadGatewayException("Cannot connect to managementportal")
            }
        }
    }

    fun readClients(): List<OAuthClient> {
        val request = Request.Builder().apply {
            url(baseUrl.resolve("api/oauth-clients")!!)
            header("Authorization", "Bearer ${ensureToken()}")
        }.build()

        return clientListReader.readValue(execute(request))
    }
}