package org.radarbase.appconfig.service

import org.radarbase.appconfig.domain.OAuthClient
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.appconfig.util.CachedSet
import org.radarbase.jersey.exception.HttpNotFoundException
import java.time.Duration
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

class ClientService(@Context private val mpClient: MPClient) {
    private val clients = CachedSet(Duration.ofHours(1), Duration.ofMinutes(5), mpClient::readClients)

    fun readClients(): Set<OAuthClient> = clients.get()

    fun ensureClient(name: String) {
        if (!contains(name)) {
            throw HttpNotFoundException("client_not_found", "OAuth client $name not found.")
        }
    }

    fun contains(clientId: String) = clients.contains(OAuthClient(clientId))
}