package org.radarbase.lang.expression

interface Function {
    val name: String
    val numberOfArguments: IntRange
    fun apply(interpreter: Interpreter, scope: List<Scope>, parameters: List<Expression>): Variable
}

abstract class AbstractFunction : Function {
    override fun toString() = name
}

class SumFunction : AbstractFunction() {
    override val name = "sum"
    override val numberOfArguments = 1..Int.MAX_VALUE
    override fun apply(interpreter: Interpreter, scope: List<Scope>, parameters: List<Expression>): Variable =
        parameters.asSequence()
            .flatMap { interpreter.interpret(scope, it).asSequence() }
            .sumOf { it.asNumber() }
            .toVariable()
}

class CountFunction : AbstractFunction() {
    override val name = "count"
    override val numberOfArguments = 1..Int.MAX_VALUE
    override fun apply(interpreter: Interpreter, scope: List<Scope>, parameters: List<Expression>) = parameters
        .sumOf {
            interpreter.interpret(scope, it)
                .asSequence()
                .count()
        }
        .toVariable()
}
