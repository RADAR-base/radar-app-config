package org.radarbase.appconfig.inject

import nl.thehyve.lang.expression.Interpreter
import javax.ws.rs.core.Context

class ClientInterpreter(
    @Context private val clientVariableResolver: ClientVariableResolver,
) {
    operator fun get(clientId: String) = Interpreter(clientVariableResolver[clientId])
}
