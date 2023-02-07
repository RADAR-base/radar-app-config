package org.radarbase.appconfig.inject

import jakarta.ws.rs.core.Context
import org.radarbase.lang.expression.Interpreter
import org.radarbase.lang.expression.VariableResolver
import java.util.function.Supplier

class InterpreterFactory(
    @Context private val variableResolver: VariableResolver,
) : Supplier<Interpreter> {
    override fun get(): Interpreter = Interpreter(variableResolver)
}
