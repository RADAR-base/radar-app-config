package org.radarbase.appconfig.service

import org.radarbase.management.client.MPOAuthClient

interface ClientService {
    fun readClients(): Collection<MPOAuthClient>

    fun ensureClient(name: String)

    operator fun contains(clientId: String): Boolean
}
