package org.radarbase.appconfig.service

import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.managementportal.MPClient
import org.radarbase.jersey.service.managementportal.MPOAuthClient
import org.radarbase.jersey.util.CachedSet
import java.time.Duration
import javax.ws.rs.core.Context

class ClientService(@Context private val mpClient: MPClient) {
    private val clients = CachedSet(Duration.ofHours(1), Duration.ofMinutes(5), mpClient::readClients)

    fun readClients(): Set<MPOAuthClient> = clients.get()

    fun ensureClient(name: String) {
        if (!contains(name)) {
            throw HttpNotFoundException("client_not_found", "OAuth client $name not found.")
        }
    }

    fun contains(clientId: String) = clients.contains(MPOAuthClient(clientId))
}
