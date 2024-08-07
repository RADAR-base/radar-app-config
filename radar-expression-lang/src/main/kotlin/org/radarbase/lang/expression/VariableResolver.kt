package org.radarbase.lang.expression

import java.math.BigDecimal

data class ResolvedVariable(val scope: Scope, val id: QualifiedId, val variable: Variable)

interface VariableResolver {
    suspend fun replace(scope: Scope, prefix: QualifiedId? = null, variables: Sequence<Pair<QualifiedId, Variable>>)
    suspend fun register(scope: Scope, id: QualifiedId, variable: Variable)
    suspend fun resolve(scopes: List<Scope>, id: QualifiedId): ResolvedVariable
    suspend fun resolveAll(scopes: List<Scope>, prefix: QualifiedId?): Sequence<ResolvedVariable>
    suspend fun list(scopes: List<Scope>, prefix: QualifiedId?): Sequence<QualifiedId>
}

suspend fun VariableResolver.register(functions: List<Function>) {
    functions.forEach {
        register(
            SimpleScope.root,
            QualifiedId("functions.${it.name}.numberOfArguments.min"),
            it.numberOfArguments.first.toVariable(),
        )
        register(
            SimpleScope.root,
            QualifiedId("functions.${it.name}.numberOfArguments.max"),
            it.numberOfArguments.last.toVariable(),
        )
    }
    register(SimpleScope.root, QualifiedId("functions"), CollectionLiteral(functions.map { it.name.toVariable() }))
}

suspend fun VariableResolver.register(scope: String, id: String, variable: Variable) {
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

    override suspend fun list(scopes: List<Scope>, prefix: QualifiedId?): Sequence<QualifiedId> {
        var refStream = scopes.asSequence()
            .flatMap { variables[it]?.keys?.asSequence() ?: emptySequence() }
            .distinct()

        val usePrefix = prefix?.names ?: listOf()
        if (usePrefix.isNotEmpty()) {
            refStream = refStream
                .filter {
                    it.names.count() >= usePrefix.count() &&
                        it.names.subList(0, usePrefix.count()) == usePrefix
                }
        }

        return refStream
    }

    override suspend fun replace(scope: Scope, prefix: QualifiedId?, variables: Sequence<Pair<QualifiedId, Variable>>) {
        val newVariables = variables.toMap()
        this.variables[scope] = if (prefix == null) {
            newVariables
        } else {
            val existingVariables = this.variables[scope]
                ?.filterKeys { k -> k.isPrefixedBy(prefix) }
                ?: mapOf()
            existingVariables + newVariables
        }.toMutableMap()
    }

    override suspend fun register(scope: Scope, id: QualifiedId, variable: Variable) {
        val root = variables.computeIfAbsent(scope) { mutableMapOf() }
        root[id] = variable
    }

    override suspend fun resolveAll(scopes: List<Scope>, prefix: QualifiedId?): Sequence<ResolvedVariable> {
        val usePrefix = QualifiedId(prefix?.names ?: listOf())

        return scopes.asSequence()
            .mapNotNull { scope -> variables[scope]?.let { scope to it } }
            .flatMap { (scope, scopeVariables) ->
                var variableStream = scopeVariables.entries.asSequence()
                if (!usePrefix.isEmpty()) {
                    variableStream = variableStream.filter { (k) -> k.isPrefixedBy(usePrefix) }
                }
                variableStream.map { (k, v) -> ResolvedVariable(scope, k, v) }
            }
    }

    override suspend fun resolve(
        scopes: List<Scope>,
        id: QualifiedId,
    ): ResolvedVariable = scopes
        .firstNotNullOfOrNull { s -> variables[s]?.get(id)?.let { ResolvedVariable(s, id, it) } }
        ?: throw UnsupportedOperationException("Unknown variable $id in scopes $scopes.")

    override fun toString(): String {
        return "DirectVariableResolver(variables=$variables)"
    }
}
