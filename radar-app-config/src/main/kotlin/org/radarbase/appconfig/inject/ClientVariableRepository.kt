package org.radarbase.appconfig.inject

import org.radarbase.appconfig.persistence.VariableRepository

interface ClientVariableRepository {
    operator fun get(clientId: String): VariableRepository
}
