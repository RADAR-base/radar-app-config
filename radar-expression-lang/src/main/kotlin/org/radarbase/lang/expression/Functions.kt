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

class ContainsFunction : AbstractFunction() {
    override val name: String = "contains"
    override val numberOfArguments: IntRange = 2 .. 2

    override fun apply(interpreter: Interpreter, scope: List<Scope>, parameters: List<Expression>): Variable =
        interpreter.interpret(scope, parameters[0]).asString()
            .contains(interpreter.interpret(scope, parameters[1]).asString(), ignoreCase = true)
            .toVariable()
}

class StartsWithFunction : AbstractFunction() {
    override val name: String = "startsWith"
    override val numberOfArguments: IntRange = 2 .. 2

    override fun apply(interpreter: Interpreter, scope: List<Scope>, parameters: List<Expression>): Variable =
        interpreter.interpret(scope, parameters[0]).asString()
            .startsWith(interpreter.interpret(scope, parameters[1]).asString(), ignoreCase = true)
            .toVariable()
}

class EndsWithFunction : AbstractFunction() {
    override val name: String = "endsWith"
    override val numberOfArguments: IntRange = 2 .. 2

    override fun apply(interpreter: Interpreter, scope: List<Scope>, parameters: List<Expression>): Variable =
        interpreter.interpret(scope, parameters[0]).asString()
            .endsWith(interpreter.interpret(scope, parameters[1]).asString(), ignoreCase = true)
            .toVariable()
}

class IncludesFunction : AbstractFunction() {
    override val name: String = "includes"
    override val numberOfArguments: IntRange = 2 .. 2

    override fun apply(interpreter: Interpreter, scope: List<Scope>, parameters: List<Expression>): Variable =
        interpreter.interpret(scope, parameters[0]).asSequence()
            .any { v -> interpreter.interpret(scope, parameters[1]).compareTo(v) == 0 }
            .toVariable()
}
