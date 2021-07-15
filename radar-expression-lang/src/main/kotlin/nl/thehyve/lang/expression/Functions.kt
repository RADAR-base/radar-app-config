package nl.thehyve.lang.expression

import java.math.BigDecimal

interface Function {
    val name: String
    val numberOfArguments: IntRange
    fun apply(interpreter: Interpreter, type: String, scope: List<Scope>, parameters: List<Expression>): Variable
}

abstract class AbstractFunction : Function {
    override fun toString() = name
}

class SumFunction : AbstractFunction() {
    override val name = "sum"
    override val numberOfArguments = 1..Int.MAX_VALUE
    override fun apply(interpreter: Interpreter, type: String, scope: List<Scope>, parameters: List<Expression>) = parameters.stream()
        .flatMap { interpreter.interpret(type, scope, it).asStream() }
        .map { it.asNumber() }
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .toVariable()
}
//
//class ListVariablesFunction : AbstractFunction() {
//    override val name = "listVariables"
//    override val numberOfArguments = 0..1
//    override fun apply(interpreter: Interpreter, type: String, scope: List<Scope>, parameters: List<Expression>): Variable {
//        val id = if (parameters.isNotEmpty()) {
//            parameters.firstOrNull() as? QualifiedId
//                ?: throw UnsupportedOperationException("Can only list variables of an ID")
//        } else null
//
//        return interpreter.variables.list(scope, id)
//            .map { it.asString().toVariable() }
//            .toList()
//            .toVariable()
//    }
//}

class CountFunction : AbstractFunction() {
    override val name = "count"
    override val numberOfArguments = 1..Int.MAX_VALUE
    override fun apply(interpreter: Interpreter, type: String, scope: List<Scope>, parameters: List<Expression>) = parameters.stream()
        .flatMap { interpreter.interpret(type, scope, it).asStream() }
        .count()
        .toVariable()
}
