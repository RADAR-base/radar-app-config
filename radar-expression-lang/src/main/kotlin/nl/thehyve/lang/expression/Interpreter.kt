package nl.thehyve.lang.expression

class InterpreterException(val expression: Expression, cause: Throwable) : RuntimeException(cause.message, cause)

class Interpreter(val variables: VariableResolver) {
    fun interpret(type: String, scope: List<Scope>, expression: Expression): Variable = expression.evaluate(type, scope)

    private fun Expression.evaluate(type: String, scope: List<Scope>): Variable {
        try {
            return when (this) {
                is OrExpression -> BooleanLiteral(left.evaluate(type, scope).asBoolean() || right.evaluate(type, scope).asBoolean())
                is AndExpression -> BooleanLiteral(
                    left.evaluate(type, scope).asBoolean() && right.evaluate(type, scope).asBoolean()
                )
                is XorExpression -> BooleanLiteral(
                    left.evaluate(type, scope).asBoolean() != right.evaluate(type, scope).asBoolean()
                )
                is EqualExpression -> BooleanLiteral(left.evaluate(type, scope).compareTo(right.evaluate(type, scope)) == 0)
                is NotEqualExpression -> BooleanLiteral(left.evaluate(type, scope).compareTo(right.evaluate(type, scope)) != 0)
                is GreaterThanOrEqualExpression -> BooleanLiteral(left.evaluate(type, scope) >= right.evaluate(type, scope))
                is GreaterThanExpression -> BooleanLiteral(left.evaluate(type, scope) > right.evaluate(type, scope))
                is LessThanExpression -> BooleanLiteral(left.evaluate(type, scope) < right.evaluate(type, scope))
                is LessThanOrEqualExpression -> BooleanLiteral(left.evaluate(type, scope) <= right.evaluate(type, scope))
                is Variable -> this
                is FunctionReference -> function.apply(this@Interpreter, type, scope, parameters)
                is QualifiedId -> variables.resolve(scope, this).variable
                is InvertExpression -> BooleanLiteral(!value.evaluate(type, scope).asBoolean())
                is NegateExpression -> value.evaluate(type, scope).asNumber().negate().toVariable()
                else -> throw UnsupportedOperationException("Cannot evaluate unknown expression $this")
            }
        } catch (ex: UnsupportedOperationException) {
            throw InterpreterException(this, ex)
        }
    }
}

interface Scope {
    val id: QualifiedId
    fun splitHead(): Pair<String?, Scope?>
    fun asString(): String = id.asString()
    operator fun plus(part: String): Scope
    fun prefixWith(prefix: String): Scope
    fun startsWith(prefix: String): Boolean
}

data class SimpleScope(override val id: QualifiedId) : Scope {
    constructor(string: String) : this(QualifiedId(string))

    override fun splitHead(): Pair<String?, Scope?> = id.splitHead()
        .let { (name, tailId) ->
            Pair(name, tailId?.let { SimpleScope(it) })
        }

    override fun toString() = id.toString()

    override fun plus(part: String): Scope = SimpleScope(id + part)

    override fun prefixWith(prefix: String): Scope = SimpleScope(id.prefixWith(prefix))
    override fun startsWith(prefix: String): Boolean = id.isPrefixedBy(QualifiedId(prefix))

    companion object {
        val root = SimpleScope(QualifiedId(listOf()))
    }
}
