package org.radarbase.appconfig.inject

import com.auth0.jwt.JWT
import org.radarcns.auth.authentication.TokenValidator
import org.radarcns.auth.config.TokenVerifierPublicKeyConfig
import org.radarbase.appconfig.Config
import org.radarbase.appconfig.auth.Auth
import org.radarbase.appconfig.auth.AuthValidator
import org.radarbase.appconfig.auth.ManagementPortalAuth
import org.radarcns.auth.exception.TokenValidationException
import java.net.URI
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Context

/** Creates a TokenValidator based on the current management portal configuration. */
class RadarTokenValidator(@Context config: Config) : AuthValidator {
    private val tokenValidator: TokenValidator = try {
        TokenValidator()
    } catch (e: RuntimeException) {
        val cfg = TokenVerifierPublicKeyConfig()
        cfg.publicKeyEndpoints = listOf(URI("${config.managementPortalUrl}/oauth/token_key"))
        cfg.resourceName = config.jwtResourceName
        TokenValidator(cfg)
    }

    override fun verify(token: String, request: ContainerRequestContext): Auth? {
        val jwt = try {
            JWT.decode(token)!!
        } catch (ex: Exception) {
            throw TokenValidationException("JWT cannot be decoded")
        }
        return ManagementPortalAuth(jwt, tokenValidator.validateAccessToken(token))
    }
}
