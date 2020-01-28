package org.radarbase.appconfig.inject

import nl.thehyve.lang.expression.VariableResolver

interface ClientVariableResolver {
    operator fun get(clientId: String): VariableResolver
}
