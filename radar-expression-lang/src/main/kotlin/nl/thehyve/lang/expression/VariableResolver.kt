package nl.thehyve.lang.expression

import me.xdrop.fuzzywuzzy.FuzzySearch
import java.math.BigDecimal
import java.util.stream.Collectors
import java.util.stream.Stream

data class ResolvedVariable(val scope: Scope, val id: QualifiedId, val variable: Variable)

interface VariableResolver {
    fun replace(scope: Scope, prefix: QualifiedId? = null, variables: Stream<Pair<QualifiedId, Variable>>)
    fun register(scope: Scope, id: QualifiedId, variable: Variable)
    fun resolve(scopes: List<Scope>, id: QualifiedId): ResolvedVariable
    fun resolveAll(scopes: List<Scope>, prefix: QualifiedId?): Stream<ResolvedVariable>
    fun list(scopes: List<Scope>, prefix: QualifiedId?): Stream<QualifiedId>
}

fun VariableResolver.register(functions: List<Function>) {
    functions.forEach {
        register(SimpleScope.root, QualifiedId("functions.${it.name}.numberOfArguments.min"), it.numberOfArguments.first.toVariable())
        register(SimpleScope.root, QualifiedId("functions.${it.name}.numberOfArguments.max"), it.numberOfArguments.last.toVariable())
    }
    register(SimpleScope.root, QualifiedId("functions"), CollectionLiteral(functions.map { it.name.toVariable() }))
}

fun VariableResolver.register(scope: String, id: String, variable: Variable) {
    register(SimpleScope(scope), QualifiedId(id), variable)
}

fun Int.toVariable(): NumberLiteral = NumberLiteral(toBigDecimal())
fun Long.toVariable(): NumberLiteral = NumberLiteral(toBigDecimal())
fun Double.toVariable(): NumberLiteral = NumberLiteral(toBigDecimal())
fun BigDecimal.toVariable(): NumberLiteral = NumberLiteral(this)
fun String.toVariable(): StringLiteral = StringLiteral(this)
fun Collection<Variable>.toVariable(): CollectionLiteral = CollectionLiteral(this)
fun Boolean.toVariable(): BooleanLiteral = BooleanLiteral(this)

class DirectVariableResolver : VariableResolver {
    override fun list(scopes: List<Scope>, prefix: QualifiedId?): Stream<QualifiedId> {
        var refStream = scopes.stream()
                .flatMap { variables[it]?.keys?.stream() ?: Stream.empty() }
                .distinct()

        val usePrefix = prefix?.names ?: listOf()
        if (usePrefix.isNotEmpty()) {
            refStream = refStream
                    .filter { it.names.count() >= usePrefix.count()
                            && it.names.subList(0, usePrefix.count()) == usePrefix }
        }

        return refStream
    }

    override fun replace(scope: Scope, prefix: QualifiedId?, variables: Stream<Pair<QualifiedId, Variable>>) {
        if (prefix == null) {
            this.variables[scope] = variables
                    .collect(Collectors.toMap<Pair<QualifiedId, Variable>, QualifiedId, Variable>({ it.first }, { it.second }))
                    .toMutableMap()
        } else {
            val variableMap = this.variables[scope]
            val newVariableMap = variableMap?.filterKeys { k -> k.isPrefixedBy(prefix) }?.toMutableMap() ?: mutableMapOf()
            newVariableMap += variables
                    .collect(Collectors.toMap<Pair<QualifiedId, Variable>, QualifiedId, Variable>({ it.first }, { it.second }))
            this.variables[scope] = newVariableMap
        }
    }

    private val variables = mutableMapOf<Scope, MutableMap<QualifiedId, Variable>>()

    override fun register(scope: Scope, id: QualifiedId, variable: Variable) {
        val root = variables[scope]
                ?: mutableMapOf<QualifiedId, Variable>().also { variables[scope] = it }

        root[id] = variable
    }

    override fun resolveAll(scopes: List<Scope>, prefix: QualifiedId?): Stream<ResolvedVariable> {
        return list(scopes, prefix)
                .map { resolve(scopes, it) }
    }

    override fun resolve(scopes: List<Scope>, id: QualifiedId): ResolvedVariable {
        val result = scopes.stream()
                .map { Pair(it, variables[it]?.get(id)) }
                .filter { it.second != null }
                .map { ResolvedVariable(it.first, id, it.second!!) }
                .findFirst()
                .orElse(null)

        if (result == null) {
            val variables = list(scopes, null)
                    .collect(Collectors.toList())

            val query = id.asString().replace('.', ' ')

            val alternatives = FuzzySearch.extractSorted(query, variables, { it.names.joinToString(" ") }, 75)
                    .take(5)

            if (alternatives.isEmpty()) {
                throw UnsupportedOperationException("Unknown variable $id in scopes $scopes.")
            } else {
                throw UnsupportedOperationException("Unknown variable $id in scopes $scopes." +
                        " Did you mean any of the following variables?\n - ${alternatives.joinToString(separator="\n - ") { it.referent.asString() }}")
            }
        } else {
            return result
        }
    }
}
