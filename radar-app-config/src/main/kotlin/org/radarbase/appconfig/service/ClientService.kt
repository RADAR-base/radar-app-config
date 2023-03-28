package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import kotlinx.coroutines.runBlocking
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.kotlin.coroutines.CacheConfig
import org.radarbase.kotlin.coroutines.CachedMap
import org.radarbase.management.client.MPClient
import org.radarbase.management.client.MPOAuthClient
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ClientService(
    @Context private val mpClient: MPClient,
    @Context private val authConfig: AuthConfig,
) {
    private val clients: CachedMap<String, MPOAuthClient> = CachedMap(
        CacheConfig(
            refreshDuration = 1.hours,
            retryDuration = 5.minutes,
        ),
    ) {
        runBlocking {
            mpClient.requestClients()
                .filter { authConfig.jwtResourceName in it.resourceIds }
                .associateBy { it.id }
        }
    }

    suspend fun readClients(): Collection<MPOAuthClient> = clients.get().values

    suspend fun ensureClient(name: String) {
        if (!clients.contains(name)) {
            throw HttpNotFoundException("client_not_found", "OAuth client $name not found.")
        }
    }

    suspend fun contains(clientId: String) = clients.contains(clientId)
}
