package nl.thehyve.lang.expression

import java.math.BigDecimal
import java.time.Instant
import java.util.Collections.unmodifiableMap

data class ResolvedVariable(val scope: Scope, val id: QualifiedId, val variable: Variable)
data class VariableSet(val type: String, val scope: Scope, val variables: Map<QualifiedId,  Variable>, val lastModifiedAt: Instant? = null)

interface VariableResolver {
    fun replace(variableSet: VariableSet)
    fun resolve(type: String, scopes: List<Scope>, id: QualifiedId): ResolvedVariable
    fun resolve(type: String, scope: Scope): VariableSet?
    fun list(type: String, scopes: List<Scope>, prefix: QualifiedId?): Sequence<QualifiedId>
}

fun Int.toVariable(): NumberLiteral = NumberLiteral(toBigDecimal())
fun Long.toVariable(): NumberLiteral = NumberLiteral(toBigDecimal())
fun Double.toVariable(): NumberLiteral = NumberLiteral(toBigDecimal())
fun BigDecimal.toVariable(): NumberLiteral = NumberLiteral(this)
fun String.toVariable(): StringLiteral = StringLiteral(this)
fun String?.toVariable(): Variable = this?.toVariable() ?: NullLiteral()
fun Collection<Variable>.toVariable(): CollectionLiteral = CollectionLiteral(this)
fun Boolean.toVariable(): BooleanLiteral = BooleanLiteral(this)

class DirectVariableResolver : VariableResolver {
    private val variables = mutableMapOf<String, MutableMap<Scope, VariableSet>>()

    override fun list(type: String, scopes: List<Scope>, prefix: QualifiedId?): Sequence<QualifiedId> {
        val typeVariables = variables[type] ?: return emptySequence()
        var refStream = scopes.asSequence()
            .flatMap { typeVariables[it]?.variables?.keys ?: emptySet() }
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

    override fun replace(variableSet: VariableSet) {
        val typeVariables = this.variables.computeIfAbsent(variableSet.type) { mutableMapOf() }
        typeVariables[variableSet.scope] = VariableSet(
            type = variableSet.type,
            scope = variableSet.scope,
            variables = unmodifiableMap(LinkedHashMap(variableSet.variables)),
            lastModifiedAt = variableSet.lastModifiedAt,
        )
    }

    override fun resolve(type: String, scope: Scope): VariableSet? = variables[type]
        ?.get(scope)

    override fun resolve(type: String, scopes: List<Scope>, id: QualifiedId): ResolvedVariable {
        val typeVariables = variables[type] ?: throw UnsupportedOperationException("Unknown variable type $type.")
        return scopes.asSequence()
            .mapNotNull { s ->
                typeVariables[s]?.variables?.get(id)
                    ?.let { ResolvedVariable(s, id, it) }
            }
            .firstOrNull()
            ?: throw UnsupportedOperationException("Unknown variable $id of type $type in scopes $scopes.")
    }

    override fun toString(): String {
        return "DirectVariableResolver(variables=$variables)"
    }
}
