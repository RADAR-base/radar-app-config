package org.radarbase.appconfig.inject

import org.radarbase.lang.expression.Interpreter
import org.radarbase.lang.expression.VariableResolver
import java.util.function.Supplier
import jakarta.ws.rs.core.Context

class InterpreterFactory(
    @Context private val variableResolver: VariableResolver,
) : Supplier<Interpreter> {
    override fun get(): Interpreter = Interpreter(variableResolver)
}
