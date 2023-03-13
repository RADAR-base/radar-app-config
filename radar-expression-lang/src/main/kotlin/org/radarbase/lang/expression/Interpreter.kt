package org.radarbase.lang.expression

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class InterpreterException(val expression: Expression, cause: Throwable) : RuntimeException(cause.message, cause)

class Interpreter(val variables: VariableResolver) {
    suspend fun interpret(scope: List<Scope>, expression: Expression): Variable = expression.evaluate(scope)

    private suspend fun Expression.evaluate(scope: List<Scope>): Variable {
        try {
            return when (this) {
                is OrExpression -> BooleanLiteral(left.evaluate(scope).asBoolean() || right.evaluate(scope).asBoolean())
                is AndExpression -> BooleanLiteral(
                    left.evaluate(scope).asBoolean() && right.evaluate(scope).asBoolean()
                )
                is XorExpression -> BooleanLiteral(
                    left.evaluate(scope).asBoolean() != right.evaluate(scope).asBoolean()
                )
                is EqualExpression -> BooleanLiteral(evaluate(scope) { l, r -> l.compareTo(r) == 0 })
                is NotEqualExpression -> BooleanLiteral(evaluate(scope) { l, r -> l.compareTo(r) != 0 })
                is GreaterThanOrEqualExpression -> BooleanLiteral(evaluate(scope) { l, r -> l >= r })
                is GreaterThanExpression -> BooleanLiteral(evaluate(scope) { l, r -> l > r })
                is LessThanExpression -> BooleanLiteral(evaluate(scope) { l, r -> l < r })
                is LessThanOrEqualExpression -> BooleanLiteral(evaluate(scope) { l, r -> l <= r })
                is Variable -> this
                is FunctionReference -> function.apply(this@Interpreter, scope, parameters)
                is QualifiedId -> variables.resolve(scope, this).variable
                is InvertExpression -> BooleanLiteral(!value.evaluate(scope).asBoolean())
                is NegateExpression -> value.evaluate(scope).asNumber().negate().toVariable()
                else -> throw UnsupportedOperationException("Cannot evaluate unknown expression $this")
            }
        } catch (ex: UnsupportedOperationException) {
            throw InterpreterException(this, ex)
        }
    }

    private suspend fun <T> BinaryExpression.evaluate(
        scope: List<Scope>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        evaluate: (Variable, Variable) -> T,
    ): T = coroutineScope {
        val leftJob = async(coroutineContext) { left.evaluate(scope) }
        val rightJob = async(coroutineContext) { right.evaluate(scope) }
        evaluate(leftJob.await(), rightJob.await())
    }
}


interface Scope {
    val id: QualifiedId
    fun splitHead(): Pair<String?, Scope?>
    fun asString(): String = id.asString()
}

data class SimpleScope(override val id: QualifiedId) : Scope {
    constructor(string: String) : this(QualifiedId(string))

    override fun splitHead(): Pair<String?, Scope?> = id.splitHead()
        .let { (name, tailId) ->
            Pair(name, tailId?.let { SimpleScope(it) })
        }

    override fun toString() = id.toString()

    companion object {
        val root = SimpleScope(QualifiedId(listOf()))
    }
}
