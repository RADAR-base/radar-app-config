package nl.thehyve.lang.expression

import java.lang.RuntimeException

class InterpreterException(val expression: Expression, cause: Throwable) : RuntimeException(cause.message, cause)

class Interpreter(val variables: VariableResolver) {
    fun interpret(scope: Scope, expression: Expression): Variable = expression.evaluate(scope)

    private fun Expression.evaluate(scope: Scope): Variable {
        try {
            return when (this) {
                is OrExpression -> BooleanLiteral(left.evaluate(scope).asBoolean() || right.evaluate(scope).asBoolean())
                is AndExpression -> BooleanLiteral(left.evaluate(scope).asBoolean() && right.evaluate(scope).asBoolean())
                is XorExpression -> BooleanLiteral(left.evaluate(scope).asBoolean() != right.evaluate(scope).asBoolean())
                is EqualExpression -> BooleanLiteral(left.evaluate(scope).compareTo(right.evaluate(scope)) == 0)
                is NotEqualExpression -> BooleanLiteral(left.evaluate(scope).compareTo(right.evaluate(scope)) != 0)
                is GreaterThanOrEqualExpression -> BooleanLiteral(left.evaluate(scope) >= right.evaluate(scope))
                is GreaterThanExpression -> BooleanLiteral(left.evaluate(scope) > right.evaluate(scope))
                is LessThanExpression -> BooleanLiteral(left.evaluate(scope) < right.evaluate(scope))
                is LessThanOrEqualExpression -> BooleanLiteral(left.evaluate(scope) <= right.evaluate(scope))
                is Variable -> this
                is FunctionReference -> function.apply(this@Interpreter, scope, parameters)
                is QualifiedId -> variables.resolve(scope, this)
                is InvertExpression -> BooleanLiteral(!value.evaluate(scope).asBoolean())
                is NegateExpression -> value.evaluate(scope).asNumber().negate().toVariable()
                else -> throw UnsupportedOperationException("Cannot evaluate unknown expression $this")
            }
        } catch (ex: UnsupportedOperationException) {
            throw InterpreterException(this, ex)
        }
    }
}

interface Scope {
    val id: QualifiedId
    fun splitHead(): Pair<String, Scope>?
}

data class SimpleScope(override val id: QualifiedId) : Scope {
    constructor(string: String) : this(QualifiedId(string))

    override fun splitHead(): Pair<String, Scope>? = id.splitHead()
            ?.let { (name, tailId) ->
                Pair(name, SimpleScope(tailId))
            }

    override fun toString() = "Scope<$id>"

    companion object {
        val root = SimpleScope(QualifiedId(listOf()))
    }
}