package org.radarbase.appconfig.inject

import nl.thehyve.lang.expression.Interpreter
import java.util.concurrent.ConcurrentHashMap
import javax.ws.rs.core.Context

class ClientInterpreter(@Context private val clientVariableResolver: ClientVariableResolver) {
    private val resolvers = ConcurrentHashMap<String, Interpreter>()

    operator fun get(clientId: String): Interpreter {
        return resolvers.computeIfAbsent(clientId) { c -> Interpreter(clientVariableResolver[c]) }
    }
}
