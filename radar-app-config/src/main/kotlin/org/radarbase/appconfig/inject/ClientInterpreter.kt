package org.radarbase.appconfig.inject

import jakarta.ws.rs.core.Context
import org.radarbase.lang.expression.Interpreter

class ClientInterpreter(
    @Context private val clientVariableResolver: ClientVariableResolver,
) {
    operator fun get(clientId: String) = Interpreter(clientVariableResolver[clientId])
}
