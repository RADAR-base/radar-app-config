/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package comparisons

import nl.thehyve.lang.expression.*
import nl.thehyve.lang.expression.Function
import kotlin.system.exitProcess

fun main() {
    val functions = listOf<Function>(
        SumFunction(),
//        ListVariablesFunction(),
        CountFunction()
    )
    val parser = ExpressionParser(functions)

    val expr = try {
        parser.parse("sum(alternative.brace.cool) == 10")
    } catch (ex: ExpressionParserException) {
        println("Failed to parse expression ${ex.expression} at position ${ex.errorOffset}: ${ex.message}")
        exitProcess(1)
    }

    println(expr)
    val resolver = DirectVariableResolver()
//    resolver.register(functions)
//    resolver.register("user", "a", 1.toVariable())
//    resolver.register("user", "b", 1.toVariable())
//    resolver.register("user", "c", 1.toVariable())
//    resolver.register("user", "r", 1.toVariable())
//    resolver.register("user", "d", 1.toVariable())
//    resolver.register("user.blootsvoets", "alternative.brace.mellow", 0.toVariable())
//    resolver.register("user.blootsvoets", "alternative.brace.cool", 10.toVariable())

    val interpreter = Interpreter(resolver)
    try {
        print(interpreter.interpret("CONFIG", listOf(SimpleScope("user.blootsvoets"), SimpleScope("user")), expr))
    } catch (ex: InterpreterException) {
        println("Failed to evaluate expression ${ex.expression}:\n\n${ex.message}")
    }
}
