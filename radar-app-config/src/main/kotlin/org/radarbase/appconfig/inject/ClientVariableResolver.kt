package org.radarbase.appconfig.inject

import org.radarbase.lang.expression.VariableResolver

interface ClientVariableResolver {
    operator fun get(clientId: String): VariableResolver
}
