package org.radarbase.lang.expression

import java.math.BigDecimal

interface Expression

interface BinaryExpression : Expression {
    val operator: String
    val left: Expression
    val right: Expression
}

abstract class AbstractBinaryExpression(override val operator: String) : BinaryExpression {
    final override fun toString() = "${left.parenString()} $operator ${right.parenString()}"
}

interface UnaryExpression : Expression {
    val operator: String
    val value: Expression
}

abstract class AbstractUnaryExpression(override val operator: String) : UnaryExpression {
    final override fun toString() = "$operator${value.parenString()}"
}

interface Variable : Comparable<Variable>, Expression {
    fun asNumber(): BigDecimal
    fun asString(): String
    fun asOptString(): String?
    fun asBoolean(): Boolean
    fun asRegularObject(): Any?
    fun asSequence(): Sequence<Variable>
}

abstract class AbstractVariable : Variable {
    override fun asNumber(): BigDecimal = throw UnsupportedOperationException("Cannot convert $this to number")
    override fun asString(): String = throw UnsupportedOperationException("Cannot convert $this to string")
    override fun asOptString(): String? = asString()
    override fun asBoolean(): Boolean = throw UnsupportedOperationException("Cannot convert $this to boolean")
    override fun asSequence(): Sequence<Variable> = sequenceOf(this)
}

fun Expression.parenString(): String = if (this is BinaryExpression) "($this)" else toString()

data class PlusExpression(override val left: Expression, override val right: Expression) :
    AbstractBinaryExpression("+")

data class MinusExpression(override val left: Expression, override val right: Expression) :
    AbstractBinaryExpression("-")

data class TimesExpression(override val left: Expression, override val right: Expression) :
    AbstractBinaryExpression("*")

data class DivisionExpression(override val left: Expression, override val right: Expression) :
    AbstractBinaryExpression("/")

data class EqualExpression(override val left: Expression, override val right: Expression) :
    AbstractBinaryExpression("==")

data class LessThanExpression(override val left: Expression, override val right: Expression) :
    AbstractBinaryExpression("<")

data class GreaterThanExpression(override val left: Expression, override val right: Expression) :
    AbstractBinaryExpression(">")

data class GreaterThanOrEqualExpression(override val left: Expression, override val right: Expression) :
    AbstractBinaryExpression(">=")

data class LessThanOrEqualExpression(override val left: Expression, override val right: Expression) :
    AbstractBinaryExpression("<=")

data class NotEqualExpression(override val left: Expression, override val right: Expression) :
    AbstractBinaryExpression("!=")

data class AndExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression("&&")

data class OrExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression("||")

data class XorExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression("^")

data class InvertExpression(override val value: Expression) : AbstractUnaryExpression("!")

data class NegateExpression(override val value: Expression) : AbstractUnaryExpression("-")

data class CollectionExpression(val values: List<Expression>) : Expression {
    override fun toString(): String = "[" + values.joinToString() + "]"
}

data class QualifiedId(val names: List<String>) : Expression {
    constructor(vararg value: String) : this(value.flatMap { it.split('#') })

    operator fun get(index: Int) = names[index]

    val size: Int
        get() = names.size

    fun head(): String = names.first()

    fun tail(startIndex: Int = 1): QualifiedId {
        require(size > startIndex) { "Cannot get suffix of single id $this" }
        return QualifiedId(names.subList(startIndex, size))
    }

    operator fun plus(name: String) = QualifiedId(names + name)
    operator fun plus(id: QualifiedId) = QualifiedId(names + id.names)

    fun asString() = names.joinToString(separator = "#")

    fun isNotEmpty(): Boolean = names.any { it.isNotEmpty() }

    override fun toString() = asString()
}

data class FunctionReference(val function: Function, val parameters: List<Expression>) : Expression {
    override fun toString() = "${function.name}(${parameters.joinToString()})"
}

data class NumberLiteral(val value: BigDecimal) : AbstractVariable() {
    override fun asString() = value.toString()

    override fun asNumber() = value

    override fun asRegularObject(): BigDecimal = value

    override fun toString() = value.toString()

    override fun compareTo(other: Variable): Int = when (other) {
        is NumberLiteral -> value.compareTo(other.value)
        is StringLiteral -> try {
            value.compareTo(BigDecimal(other.value))
        } catch (ex: NumberFormatException) {
            value.toString().compareTo(other.value)
        }
        else -> throw UnsupportedOperationException("Cannot compare $this with $other")
    }
}

class NullLiteral : AbstractVariable() {
    override fun compareTo(other: Variable): Int =
        if (other is NullLiteral) 0 else throw UnsupportedOperationException("Cannot compare null to other value")

    override fun asRegularObject(): Any? = null

    override fun asOptString(): String? = null

    override fun toString() = "null"
}

data class BooleanLiteral(val value: Boolean) : AbstractVariable() {
    override fun asString() = value.toString()

    override fun asBoolean() = value

    override fun asRegularObject(): Boolean = value

    override fun toString() = value.toString()

    override fun compareTo(other: Variable): Int = when (other) {
        is BooleanLiteral -> value.compareTo(other.value)
        is StringLiteral -> try {
            value.compareTo(other.asBoolean())
        } catch (ex: NumberFormatException) {
            value.toString().compareTo(other.value)
        }
        else -> throw UnsupportedOperationException("Cannot compare $this with $other")
    }

    companion object {
        val FALSE = BooleanLiteral(false)
        val TRUE = BooleanLiteral(true)
    }
}

fun String.toBooleanLiteral(): BooleanLiteral? = when {
    equals("true", ignoreCase = true) -> BooleanLiteral.TRUE
    equals("false", ignoreCase = true) -> BooleanLiteral.FALSE
    else -> null
}

data class StringLiteral(val value: String) : AbstractVariable() {
    override fun asString() = value

    override fun asBoolean() = value.toBooleanLiteral()?.asBoolean()
        ?: throw UnsupportedOperationException("Cannot convert $this to boolean.")

    override fun asRegularObject(): String = value

    override fun asNumber(): BigDecimal = BigDecimal(value)

    override fun asSequence(): Sequence<Variable> = value.split(' ', '\t', ',', ';')
        .asSequence()
        .filter { it.isNotEmpty() }
        .map { it.toVariable() }

    override fun toString() = value
        .replace("\\", "\\\\")
        .replace("'", "\\'")

    override fun compareTo(other: Variable): Int = when (other) {
        is BooleanLiteral -> -other.compareTo(this)
        is NumberLiteral -> -other.compareTo(this)
        is StringLiteral -> value.compareTo(other.value)
        is CollectionLiteral -> -other.compareTo(this)
        else -> throw UnsupportedOperationException("Cannot compare $this with $other")
    }
}

fun String.toUnescapedStringLiteral(): StringLiteral {
    val quoteSymbol = if (this[0] == '\'') "'" else "\""
    return StringLiteral(
        substring(1, count() - 1)
            .replace("\\\\", "\\\\/")
            .replace("\\$quoteSymbol", quoteSymbol)
            .replace("\\\\/", "\\")
    )
}

data class CollectionLiteral(val values: Collection<Variable>) : Variable {
    override fun asOptString(): String = asString()

    override fun asNumber(): BigDecimal = throw UnsupportedOperationException("Cannot convert $this to a number.")

    override fun asString() = values.joinToString(separator = " ") { it.asString() }

    override fun asRegularObject(): List<Any?> = values.map { it.asRegularObject() }

    override fun asBoolean() = throw UnsupportedOperationException("Cannot convert $this to a boolaean.")

    override fun asSequence() = values.asSequence()

    override fun toString() = values.toString()

    override fun compareTo(other: Variable): Int {
        val comparisonSequence = if (other is CollectionLiteral) {
            val terminator = Any()
            (asSequence() + terminator)
                .zip(other.asSequence() + terminator)
                .map { (a, b) ->
                    when {
                        a === b -> 0
                        a === terminator -> -1
                        b === terminator -> 1
                        else -> (a as Variable).compareTo(b as Variable)
                    }
                }
        } else {
            asSequence()
                .map { it.compareTo(other) }
        }
        return comparisonSequence
            .firstOrNull { it != 0 }
            ?: 0
    }
}
