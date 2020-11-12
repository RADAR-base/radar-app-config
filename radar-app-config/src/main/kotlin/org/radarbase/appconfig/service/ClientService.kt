package org.radarbase.appconfig.service

import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.managementportal.MPClient
import org.radarbase.jersey.service.managementportal.MPOAuthClient
import org.radarbase.jersey.util.CacheConfig
import org.radarbase.jersey.util.CachedMap
import java.time.Duration
import javax.ws.rs.core.Context

class ClientService(@Context private val mpClient: MPClient) {
    private val clients = CachedMap(
        CacheConfig(
            refreshDuration = Duration.ofHours(1),
            retryDuration = Duration.ofMinutes(5)
        )
    ) {
        mpClient.readClients().map { it.id to it }.toMap(HashMap())
    }

    fun readClients(): Collection<MPOAuthClient> = clients.get().values

    fun ensureClient(name: String) {
        if (name !in clients) {
            throw HttpNotFoundException("client_not_found", "OAuth client $name not found.")
        }
    }

    operator fun contains(clientId: String) = clientId in clients
}
