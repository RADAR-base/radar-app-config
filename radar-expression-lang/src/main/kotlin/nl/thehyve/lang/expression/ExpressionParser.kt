package nl.thehyve.lang.expression

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import nl.thehyve.lang.expression.antlr.ComparisonLexer
import nl.thehyve.lang.expression.antlr.ComparisonParser
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.math.BigDecimal
import java.text.ParseException

@JsonDeserialize(using = ExpressionDeserializer::class)
class ExpressionParser(functions: List<Function>) {
    private val indexedFunctions: Map<String, Function> = functions
            .map { it.name to it }
            .toMap()

    fun parse(value: String) = parse(ByteArrayInputStream(value.toByteArray()))

    fun parse(input: InputStream): Expression {
        val lexer = ComparisonLexer(CharStreams.fromStream(input))
        val context = ParserContext(ComparisonParser(CommonTokenStream(lexer)))
        return context.parse()
    }

    private inner class ParserContext(private val parser: ComparisonParser) {
        fun parse(): Expression = parser.expression().toModel()


        private fun ComparisonParser.ExpressionContext.toModel() : Expression = when (this) {
            is ComparisonParser.BinaryOperationContext -> toModel()
            is ComparisonParser.CombinationOperationContext -> toModel()
            is ComparisonParser.UnaryOperationContext -> toModel()
            is ComparisonParser.ParenExpressionContext -> expression().toModel()
            is ComparisonParser.NullLiteralContext -> NullLiteral()
            is ComparisonParser.BooleanLiteralContext -> text.toBooleanLiteral() ?: throw toException("Cannot map string to boolean literal.")
            is ComparisonParser.StringLiteralContext -> text.toUnescapedStringLiteral()
            is ComparisonParser.DecimalLiteralContext,
            is ComparisonParser.IntegerLiteralContext -> BigDecimal(text).toVariable()
            is ComparisonParser.FunctionExpressionContext -> function().toModel()
            is ComparisonParser.QualifiedIdExpressionContext -> QualifiedId(text)
            else -> throw toException("Cannot map expression $javaClass at ${toInfoString(parser)}")
        }

        private fun ComparisonParser.UnaryOperationContext.toModel(): UnaryExpression =when (operation.type) {
            ComparisonLexer.MINUS -> NegateExpression(expression().toModel())
            ComparisonLexer.NOT -> InvertExpression(expression().toModel())
            else -> throw toException("Cannot map unknown unary operator ${operation.text}")
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
                else -> throw toException("Cannot map binary operation $comparator at ${toInfoString(parser)}")
            }
        }

        private fun ComparisonParser.CombinationOperationContext.toModel(): BinaryExpression {
            val leftExpression = left.toModel()
            val rightExpression = right.toModel()
            return when (this.comparator.type) {
                ComparisonLexer.AND -> AndExpression(left = leftExpression, right = rightExpression)
                ComparisonLexer.OR -> OrExpression(left = leftExpression, right = rightExpression)
                ComparisonLexer.XOR -> XorExpression(left = leftExpression, right = rightExpression)
                else -> throw toException("Cannot map binary operation $comparator at ${toInfoString(parser)}")
            }
        }

        private fun ComparisonParser.FunctionContext.toModel(): FunctionReference {
            val functionName = ID().text
            val function = indexedFunctions[functionName]

            if (function == null) {
                val alternatives = FuzzySearch.extractSorted(functionName, indexedFunctions.keys, 60)
                        .take(5)
                if (alternatives.isEmpty()) {
                    throw toException("Unknown function ${ID().text}.")
                } else {
                    throw toException("Unknown function ${ID().text}. Did you mean any of the following functions:" +
                            "\n - ${alternatives.joinToString(separator = "\n - ") { it.string }}")
                }
            }
            val expressions = expression()

            if (!function.numberOfArguments.contains(expressions.count())) {
                throw toException("Number of parameters to function $functionName is ${expressions.count()} but should be ${function.numberOfArguments}.")
            }

            return FunctionReference(function = function, parameters=expressions.map { it.toModel() })
        }
    }
}

class ExpressionParserException(message: String, errorOffset: Int, val expression: String) : ParseException(message, errorOffset)

private fun ParserRuleContext.toException(message: String) = ExpressionParserException(message, start.startIndex, text)
