package org.radarbase.appconfig.service

import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.management.client.MPOAuthClient

class FixedClientService(
    private val clients: Map<String, MPOAuthClient>
) : ClientService {
    override fun readClients(): Collection<MPOAuthClient> = clients.values

    override fun ensureClient(name: String) {
        if (name !in clients) {
            throw HttpNotFoundException("client_not_found", "OAuth client $name not found.")
        }
    }

    override operator fun contains(clientId: String) = clientId in clients
}
