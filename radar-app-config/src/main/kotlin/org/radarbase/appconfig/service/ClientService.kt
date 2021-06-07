package org.radarbase.appconfig.service

import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.util.CacheConfig
import org.radarbase.jersey.util.CachedMap
import java.time.Duration
import jakarta.ws.rs.core.Context
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.management.client.MPClient
import org.radarbase.management.client.MPOAuthClient

class ClientService(
    @Context private val mpClient: MPClient,
    @Context private val authConfig: AuthConfig,
) {
    private val clients = CachedMap(
        CacheConfig(
            refreshDuration = Duration.ofHours(1),
            retryDuration = Duration.ofMinutes(5)
        )
    ) {
        mpClient.requestClients()
            .filter { authConfig.jwtResourceName in it.resourceIds }
            .associateBy { it.id }
    }

    fun readClients(): Collection<MPOAuthClient> = clients.get().values

    fun ensureClient(name: String) {
        if (name !in clients) {
            throw HttpNotFoundException("client_not_found", "OAuth client $name not found.")
        }
    }

    operator fun contains(clientId: String) = clientId in clients
}
