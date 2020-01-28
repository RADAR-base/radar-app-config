package org.radarbase.appconfig.inject

import nl.thehyve.lang.expression.Interpreter
import nl.thehyve.lang.expression.VariableResolver
import java.util.function.Supplier
import javax.ws.rs.core.Context

class InterpreterFactory(
        @Context private val variableResolver: VariableResolver
): Supplier<Interpreter> {
    override fun get(): Interpreter = Interpreter(variableResolver)
}
