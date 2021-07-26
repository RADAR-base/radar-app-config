package org.radarbase.appconfig.inject

import org.radarbase.lang.expression.Interpreter
import jakarta.ws.rs.core.Context

class ClientInterpreter(
    @Context private val clientVariableResolver: ClientVariableResolver,
) {
    operator fun get(clientId: String) = Interpreter(clientVariableResolver[clientId])
}
