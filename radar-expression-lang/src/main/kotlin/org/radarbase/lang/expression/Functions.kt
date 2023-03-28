package org.radarbase.lang.expression

import kotlinx.coroutines.coroutineScope
import org.radarbase.kotlin.coroutines.forkJoin

interface Function {
    val name: String
    val numberOfArguments: IntRange
    suspend fun apply(
        interpreter: Interpreter,
        scope: List<Scope>,
        parameters: List<Expression>,
    ): Variable
}

abstract class AbstractFunction : Function {
    override fun toString() = name
}

class SumFunction : AbstractFunction() {
    override val name = "sum"
    override val numberOfArguments = 1..Int.MAX_VALUE
    override suspend fun apply(
        interpreter: Interpreter,
        scope: List<Scope>,
        parameters: List<Expression>,
    ): Variable = coroutineScope {
        parameters
            .forkJoin {
                interpreter.interpret(scope, it)
            }
            .sumOf { upper -> upper.asSequence().sumOf { it.asNumber() } }
            .toVariable()
    }
}

class ListVariablesFunction : AbstractFunction() {
    override val name = "listVariables"
    override val numberOfArguments = 0..1
    override suspend fun apply(
        interpreter: Interpreter,
        scope: List<Scope>,
        parameters: List<Expression>,
    ): Variable {
        val id = if (parameters.isNotEmpty()) {
            parameters.firstOrNull() as? QualifiedId
                ?: throw UnsupportedOperationException("Can only list variables of an ID")
        } else {
            null
        }

        return interpreter.variables.list(scope, id)
            .map { it.asString().toVariable() }
            .toList()
            .toVariable()
    }
}

class CountFunction : AbstractFunction() {
    override val name = "count"
    override val numberOfArguments = 1..Int.MAX_VALUE
    override suspend fun apply(
        interpreter: Interpreter,
        scope: List<Scope>,
        parameters: List<Expression>,
    ) = coroutineScope {
        parameters
            .forkJoin { interpreter.interpret(scope, it) }
            .sumOf { it.asSequence().count() }
            .toVariable()
    }
}
