package nl.thehyve.lang.expression

import nl.thehyve.lang.expression.antlr.ComparisonLexer
import nl.thehyve.lang.expression.antlr.ComparisonParser
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.math.BigDecimal
import java.text.ParseException

class ExpressionParser(input: InputStream, functions: List<Function>) {
    private val parser: ComparisonParser
    private val indexedFunctions: Map<String, Function> = functions
            .map { it.name to it }
            .toMap()

    init {
        val lexer = ComparisonLexer(ANTLRInputStream(input))
        parser = ComparisonParser(CommonTokenStream(lexer))
    }

    constructor(value: String, functions: List<Function>) : this(ByteArrayInputStream(value.toByteArray()), functions)

    fun parse(): Expression = parser.expression().toModel()

    private fun ComparisonParser.ExpressionContext.toModel() : Expression = when (this) {
        is ComparisonParser.BinaryOperationContext -> toModel()
        is ComparisonParser.CombinationOperationContext -> toModel()
        is ComparisonParser.UnaryOperationContext -> toModel()
        is ComparisonParser.ParenExpressionContext -> expression().toModel()
        is ComparisonParser.NullLiteralContext -> NullLiteral()
        is ComparisonParser.BooleanLiteralContext -> BooleanLiteral.parse(text) ?: throw makeException("Cannot map string to boolean literal.")
        is ComparisonParser.StringLiteralContext -> StringLiteral.parseEscapedString(text)
        is ComparisonParser.DecimalLiteralContext,
        is ComparisonParser.IntegerLiteralContext -> BigDecimal(text).toVariable()
        is ComparisonParser.FunctionExpressionContext -> function().toModel()
        is ComparisonParser.QualifiedIdExpressionContext -> QualifiedId(text)
        else -> throw makeException("Cannot map expression $javaClass at ${toInfoString(parser)}")
    }

    private fun ComparisonParser.UnaryOperationContext.toModel(): UnaryExpression =when (operation.type) {
        ComparisonLexer.MINUS -> NegateExpression(expression().toModel())
        ComparisonLexer.NOT -> InvertExpression(expression().toModel())
        else -> throw makeException("Cannot map unknown unary operator ${operation.text}")
    }

    private fun ComparisonParser.BinaryOperationContext.toModel(): BinaryExpression {
        val leftExpression = left.toModel()
        val rightExpression = right.toModel()
        return when (this.comparator.type) {
            ComparisonLexer.EQ -> EqualExpression(left = leftExpression, right = rightExpression)
            ComparisonLexer.NE -> NotEqualExpression(left = leftExpression, right = rightExpression)
            ComparisonLexer.GT -> GreaterThanExpression(left = leftExpression, right = rightExpression)
            ComparisonLexer.GTE -> GreaterThanOrEqualExpression(left = leftExpression, right = rightExpression)
            ComparisonLexer.LT -> LessThanExpression(left = leftExpression, right = rightExpression)
            ComparisonLexer.LTE -> LessThanOrEqualExpression(left = leftExpression, right = rightExpression)
            else -> throw makeException("Cannot map binary operation $comparator at ${toInfoString(parser)}")
        }
    }

    private fun ComparisonParser.CombinationOperationContext.toModel(): BinaryExpression {
        val leftExpression = left.toModel()
        val rightExpression = right.toModel()
        return when (this.comparator.type) {
            ComparisonLexer.AND -> AndExpression(left = leftExpression, right = rightExpression)
            ComparisonLexer.OR -> OrExpression(left = leftExpression, right = rightExpression)
            ComparisonLexer.XOR -> XorExpression(left = leftExpression, right = rightExpression)
            else -> throw makeException("Cannot map binary operation $comparator at ${toInfoString(parser)}")
        }
    }

    private fun ComparisonParser.FunctionContext.toModel(): FunctionReference {
        val functionName = ID().text
        val function = indexedFunctions[functionName]

        if (function == null) {
            val alternatives = FuzzySearch.extractSorted(functionName, indexedFunctions.keys, 60)
                    .take(5)
            if (alternatives.isEmpty()) {
                throw makeException("Unknown function ${ID().text}.")
            } else {
                throw makeException("Unknown function ${ID().text}. Did you mean any of the following functions:" +
                        "\n - ${alternatives.joinToString(separator = "\n - ") { it.string }}")
            }
        }
        val expressions = expression()

        if (!function.numberOfArguments.contains(expressions.count())) {
            throw makeException("Number of parameters to function $functionName is ${expressions.count()} but should be ${function.numberOfArguments}.")
        }

        return FunctionReference(function = function, parameters=expressions.map { it.toModel() })
    }

    private fun ParserRuleContext.makeException(message: String) = ExpressionParserException(message, start.startIndex, text)
}

class ExpressionParserException(message: String, errorOffset: Int, val expression: String) : ParseException(message, errorOffset)