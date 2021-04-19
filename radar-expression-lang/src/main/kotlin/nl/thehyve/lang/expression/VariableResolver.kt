package nl.thehyve.lang.expression

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
        register(
            SimpleScope.root,
            QualifiedId("functions.${it.name}.numberOfArguments.min"),
            it.numberOfArguments.first.toVariable()
        )
        register(
            SimpleScope.root,
            QualifiedId("functions.${it.name}.numberOfArguments.max"),
            it.numberOfArguments.last.toVariable()
        )
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
    private val variables = mutableMapOf<Scope, MutableMap<QualifiedId, Variable>>()

    override fun list(scopes: List<Scope>, prefix: QualifiedId?): Stream<QualifiedId> {
        var refStream = scopes.stream()
            .flatMap { variables[it]?.keys?.stream() ?: Stream.empty() }
            .distinct()

        val usePrefix = prefix?.names ?: listOf()
        if (usePrefix.isNotEmpty()) {
            refStream = refStream
                .filter {
                    it.names.count() >= usePrefix.count()
                        && it.names.subList(0, usePrefix.count()) == usePrefix
                }
        }

        return refStream
    }

    override fun replace(scope: Scope, prefix: QualifiedId?, variables: Stream<Pair<QualifiedId, Variable>>) {
        val newVariables = variables
            .collect(Collectors.toMap({ it.first }, { it.second }))
        this.variables[scope] = if (prefix == null) {
            newVariables
        } else {
            val existingVariables = this.variables[scope]
                ?.filterKeys { k -> k.isPrefixedBy(prefix) }
                ?: mapOf()
            existingVariables + newVariables
        }.toMutableMap()
    }

    override fun register(scope: Scope, id: QualifiedId, variable: Variable) {
        val root = variables.computeIfAbsent(scope) { mutableMapOf() }
        root[id] = variable
    }

    override fun resolveAll(scopes: List<Scope>, prefix: QualifiedId?): Stream<ResolvedVariable> {
        val usePrefix = QualifiedId(prefix?.names ?: listOf())

        return scopes.stream()
            .flatMap { scope ->
                var variableStream = variables[scope]?.entries?.stream()

                if (!usePrefix.isEmpty()) {
                    variableStream = variableStream
                        ?.filter { it.key.isPrefixedBy(usePrefix) }
                }

                variableStream
                    ?.map { ResolvedVariable(scope, it.key, it.value) }
                    ?: Stream.empty()
            }
    }

    override fun resolve(scopes: List<Scope>, id: QualifiedId): ResolvedVariable {
        return scopes.asSequence()
            .mapNotNull { s -> variables[s]?.get(id)?.let { ResolvedVariable(s, id, it) }}
            .firstOrNull()
            ?: throw UnsupportedOperationException("Unknown variable $id in scopes $scopes.")
    }

    override fun toString(): String {
        return "DirectVariableResolver(variables=$variables)"
    }
}
