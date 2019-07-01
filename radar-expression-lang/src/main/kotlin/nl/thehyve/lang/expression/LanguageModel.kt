package nl.thehyve.lang.expression

import java.math.BigDecimal
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.NoSuchElementException
import kotlin.math.min


interface Expression

interface BinaryExpression : Expression {
    val left: Expression
    val right: Expression
}

interface UnaryExpression : Expression {
    val value: Expression
}

interface Variable : Comparable<Variable>, Expression {
    fun asNumber(): BigDecimal
    fun asString(): String
    fun asBoolean(): Boolean
    fun asStream(): Stream<Variable>
}

fun Expression.parenString(): String = if (this is BinaryExpression) "($this)" else toString()

data class EqualExpression(override val left: Expression, override val right: Expression) : BinaryExpression {
    override fun toString() = "${left.parenString()} == ${right.parenString()}"
}
data class LessThanExpression(override val left: Expression, override val right: Expression) : BinaryExpression {
    override fun toString() = "${left.parenString()} < ${right.parenString()}"
}
data class GreaterThanExpression(override val left: Expression, override val right: Expression) : BinaryExpression {
    override fun toString() = "${left.parenString()} > ${right.parenString()}"
}
data class GreaterThanOrEqualExpression(override val left: Expression, override val right: Expression) : BinaryExpression {
    override fun toString() = "${left.parenString()} >= ${right.parenString()}"
}
data class LessThanOrEqualExpression(override val left: Expression, override val right: Expression) : BinaryExpression {
    override fun toString() = "${left.parenString()} <= ${right.parenString()}"
}
data class NotEqualExpression(override val left: Expression, override val right: Expression) : BinaryExpression {
    override fun toString() = "${left.parenString()} != ${right.parenString()}"
}
data class AndExpression(override val left: Expression, override val right: Expression) : BinaryExpression {
    override fun toString() = "${left.parenString()} && ${right.parenString()}"
}
data class OrExpression(override val left: Expression, override val right: Expression) : BinaryExpression {
    override fun toString() = "${left.parenString()} || ${right.parenString()}"
}
data class XorExpression(override val left: Expression, override val right: Expression) : BinaryExpression {
    override fun toString() = "${left.parenString()} ^ ${right.parenString()}"
}

data class InvertExpression(override val value: Expression) : UnaryExpression {
    override fun toString() = "!${value.parenString()}"
}

data class NegateExpression(override val value: Expression): UnaryExpression {
    override fun toString() = "-${value.parenString()}"
}

data class QualifiedId(val names: List<String>) : Expression {
    constructor(value: String) : this(value.split('.'))

    fun splitHead(): Pair<String, QualifiedId>? = if (names.isNotEmpty()) {
        Pair(names[0], QualifiedId(names.subList(1, names.count())))
    } else null

    fun asString() = names.joinToString(separator = ".")

    override fun toString() = asString()
}
data class FunctionReference(val function: Function, val parameters: List<Expression>) : Expression {
    override fun toString() = "${function.name}(${parameters.joinToString()})"
}

data class NumberLiteral(val value: BigDecimal) : Expression, Variable {
    override fun asString() = value.toString()

    override fun asBoolean() = throw UnsupportedOperationException("Cannot treat a number as a boolean")

    override fun asStream(): Stream<Variable> = Stream.of(this)

    override fun asNumber() = value

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

class NullLiteral : Variable {
    override fun asNumber() = throw UnsupportedOperationException("Cannot convert null to number")

    override fun asString() = throw UnsupportedOperationException("Cannot convert null to string")

    override fun asBoolean() = throw UnsupportedOperationException("Cannot convert null to boolean")

    override fun asStream(): Stream<Variable> = Stream.of(this)

    override fun compareTo(other: Variable): Int = if (other is NullLiteral) 0 else throw UnsupportedOperationException("Cannot compare null to other value")

    override fun toString() = "null"
}

data class BooleanLiteral(val value: Boolean) : Variable {
    override fun asString() = value.toString()

    override fun asBoolean() = value

    override fun asStream(): Stream<Variable> = Stream.of(this)

    override fun asNumber(): BigDecimal = throw UnsupportedOperationException("Cannot treat a boolean as a number")

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
        fun parse(value: String): BooleanLiteral? = when {
            value.equals("true", ignoreCase = true) -> BooleanLiteral(true)
            value.equals("false", ignoreCase = true) -> BooleanLiteral(false)
            else -> null
        }
    }
}

data class StringLiteral(val value: String) : Variable {
    override fun asString() = value

    override fun asBoolean() = BooleanLiteral.parse(value)?.asBoolean()
            ?: throw UnsupportedOperationException("Cannot convert $this to boolean.")

    override fun asStream(): Stream<Variable> = Stream.of(this)

    override fun asNumber(): BigDecimal = BigDecimal(value)

    override fun toString() = "'${value
            .replace("\\", "\\\\")
            .replace("'", "\\'")}'"

    override fun compareTo(other: Variable): Int = when (other) {
        is BooleanLiteral -> -other.compareTo(this)
        is NumberLiteral -> -other.compareTo(this)
        is StringLiteral -> value.compareTo(other.value)
        is CollectionLiteral -> -other.compareTo(this)
        else -> throw UnsupportedOperationException("Cannot compare $this with $other")
    }

    companion object {
        fun parseEscapedString(value: String): StringLiteral {
            return StringLiteral(if (value[0] == '\'') {
                value.substring(1, value.count() - 1)
                        .replace("\\\\", "\\\\/")
                        .replace("\\'", "'")
                        .replace("\\\\/", "\\")
            } else {
                value.substring(1, value.count() - 1)
                        .replace("\\\\", "\\\\/")
                        .replace("\\\"", "\"")
                        .replace("\\\\/", "\\")
            })
        }
    }
}

data class CollectionLiteral(val values: Collection<Variable>): Variable {
    override fun asNumber(): BigDecimal = throw UnsupportedOperationException("Cannot convert $this to a number.")

    override fun asString() = throw UnsupportedOperationException("Cannot convert $this to a string.")

    override fun asBoolean() = throw UnsupportedOperationException("Cannot convert $this to a boolaean.")

    override fun asStream() = values.stream()

    override fun toString() = values.toString()

    override fun compareTo(other: Variable): Int {
        val comparisonStream = if (other is CollectionLiteral) {
            asStream().zipOrNull(other.asStream())
                    .map { (a, b) ->
                        when {
                            a == null -> -1
                            b == null -> 1
                            else -> a.compareTo(b)
                        }
                    }
        } else {
            asStream().map { it.compareTo(other) }
        }

        return comparisonStream
                .filter { it != 0 }
                .findFirst()
                .orElse(0)

    }
}

fun <A, B> Stream<A>.zipOrNull(other: Stream<out B>): Stream<Pair<A?, B?>> {
    val splitA = spliterator()
    val splitB = other.spliterator()

    // Zipping looses SORTED characteristic
    val characteristics = splitA.characteristics() and splitB.characteristics() and
            Spliterator.SORTED.inv()

    val zipSize = min(splitA.exactSizeIfKnown, splitB.exactSizeIfKnown)

    val iterA = Spliterators.iterator(splitA)
    val iterB = Spliterators.iterator(splitB)
    val iterPair = object : Iterator<Pair<A?, B?>> {
        override fun hasNext() = iterA.hasNext() || iterB.hasNext()

        override fun next(): Pair<A?, B?> {
            val nextA = iterA.hasNext()
            val nextB = iterB.hasNext()
            return when {
                nextA && nextB -> Pair(iterA.next(), iterB.next())
                nextA -> Pair(iterA.next(), null)
                nextB -> Pair(null, iterB.next())
                else -> throw NoSuchElementException()
            }
        }
    }

    val split = Spliterators.spliterator(iterPair, zipSize, characteristics)
    return StreamSupport.stream(split, isParallel || other.isParallel)
}