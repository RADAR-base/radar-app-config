package nl.thehyve.lang.expression

import java.math.BigDecimal
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.NoSuchElementException
import kotlin.math.min


interface Expression

interface BinaryExpression : Expression {
    val operator: String
    val left: Expression
    val right: Expression
}

abstract class AbstractBinaryExpression(override val operator: String): BinaryExpression {
    final override fun toString() = "${left.parenString()} $operator ${right.parenString()}"
}

interface UnaryExpression : Expression {
    val operator: String
    val value: Expression
}

abstract class AbstractUnaryExpression(override val operator: String): UnaryExpression {
    final override fun toString() = "$operator${value.parenString()}"
}

interface Variable : Comparable<Variable>, Expression {
    fun asNumber(): BigDecimal
    fun asString(): String
    fun asOptString(): String?
    fun asBoolean(): Boolean
    fun asStream(): Stream<Variable>
}

abstract class AbstractVariable: Variable {
    override fun asNumber(): BigDecimal = throw UnsupportedOperationException("Cannot convert $this to number")
    override fun asString(): String = throw UnsupportedOperationException("Cannot convert $this to string")
    override fun asOptString(): String? = asString()
    override fun asBoolean(): Boolean = throw UnsupportedOperationException("Cannot convert $this to boolean")
    override fun asStream(): Stream<Variable> = Stream.of(this)
}

fun Expression.parenString(): String = if (this is BinaryExpression) "($this)" else toString()

data class EqualExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression("==")

data class LessThanExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression("<")

data class GreaterThanExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression(">")

data class GreaterThanOrEqualExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression(">=")

data class LessThanOrEqualExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression("<=")

data class NotEqualExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression("!=")

data class AndExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression("&&")

data class OrExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression("||")

data class XorExpression(override val left: Expression, override val right: Expression) : AbstractBinaryExpression("^")

data class InvertExpression(override val value: Expression) : AbstractUnaryExpression("!")

data class NegateExpression(override val value: Expression): AbstractUnaryExpression("-")

data class QualifiedId(val names: List<String>) : Expression {
    constructor(vararg value: String) : this(value.toList().flatMap { it.split('.') })

    fun splitHead(): Pair<String, QualifiedId>? = if (names.isNotEmpty()) {
        Pair(names[0], QualifiedId(names.subList(1, names.count())))
    } else null

    operator fun plus(name: String) = QualifiedId(names + name)
    operator fun plus(id: QualifiedId) = QualifiedId(names + id.names)

    fun prefixWith(prefix: String) = QualifiedId(prefix + names)

    fun asString() = names.joinToString(separator = ".")

    override fun toString() = asString()
}

data class FunctionReference(val function: Function, val parameters: List<Expression>) : Expression {
    override fun toString() = "${function.name}(${parameters.joinToString()})"
}

data class NumberLiteral(val value: BigDecimal) : AbstractVariable() {
    override fun asString() = value.toString()

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

class NullLiteral : AbstractVariable() {
    override fun compareTo(other: Variable): Int = if (other is NullLiteral) 0 else throw UnsupportedOperationException("Cannot compare null to other value")

    override fun asOptString(): String? = null

    override fun toString() = "null"
}

data class BooleanLiteral(val value: Boolean) : AbstractVariable() {
    override fun asString() = value.toString()

    override fun asBoolean() = value

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

data class StringLiteral(val value: String) : AbstractVariable() {
    override fun asString() = value

    override fun asBoolean() = BooleanLiteral.parse(value)?.asBoolean()
            ?: throw UnsupportedOperationException("Cannot convert $this to boolean.")

    override fun asNumber(): BigDecimal = BigDecimal(value)

    override fun asStream(): Stream<Variable> = value.split(" ")
            .stream()
            .filter { it.isNotEmpty() }
            .map { it.toVariable() }

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
    override fun asOptString(): String? = asString()

    override fun asNumber(): BigDecimal = throw UnsupportedOperationException("Cannot convert $this to a number.")

    override fun asString() = values.joinToString(separator = " ") { it.asString() }

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