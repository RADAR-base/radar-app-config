package org.radarbase.appconfig.inject

import org.radarbase.appconfig.auth.Auth
import org.radarbase.appconfig.auth.RadarSecurityContext
import java.util.function.Supplier
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Context

/** Generates radar tokens from the security context. */
class AuthFactory : Supplier<Auth> {
    @Context
    private lateinit var context: ContainerRequestContext

    override fun get() = (context.securityContext as? RadarSecurityContext)?.auth
                ?: throw IllegalStateException("Created null wrapper")
}
