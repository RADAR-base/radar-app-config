package org.radarbase.lang.expression

import java.math.BigDecimal
import java.time.Instant
import java.util.Collections.unmodifiableMap

data class ResolvedVariable(val scope: Scope, val id: QualifiedId, val variable: Variable)
data class VariableSet(val id: Long?, val scope: Scope, val variables: Map<QualifiedId, Variable>, val lastModifiedAt: Instant? = null)

interface VariableRepository : VariableResolver {
    fun update(variableSet: VariableSet): UpdateResult
    fun resolve(scope: Scope): VariableSet?
    fun get(id: Long): VariableSet?
}

interface VariableResolver {
    fun resolve(scopes: List<Scope>, id: QualifiedId): ResolvedVariable
}

data class UpdateResult(val id: Long, val didUpdate: Boolean)

fun Int.toVariable(): NumberLiteral = NumberLiteral(toBigDecimal())
fun Long.toVariable(): NumberLiteral = NumberLiteral(toBigDecimal())
fun Double.toVariable(): NumberLiteral = NumberLiteral(toBigDecimal())
fun BigDecimal.toVariable(): NumberLiteral = NumberLiteral(this)
fun String.toVariable(): StringLiteral = StringLiteral(this)
fun String?.toVariable(): Variable = this?.toVariable() ?: NullLiteral()
fun Collection<Variable>.toVariable(): CollectionLiteral = CollectionLiteral(this)
fun Boolean.toVariable(): BooleanLiteral = BooleanLiteral(this)

class DirectVariableRepository : VariableRepository {
    private var nextId: Long = 1L
    private val variables = mutableMapOf<Scope, VariableSet>()

    override fun update(variableSet: VariableSet): UpdateResult {
        val oldValue = variables[variableSet.scope]
        return if (oldValue == null || oldValue.variables != variableSet.variables) {
            val id = nextId
            nextId += 1L
            variables[variableSet.scope] = VariableSet(
                id = id,
                scope = variableSet.scope,
                variables = unmodifiableMap(LinkedHashMap(variableSet.variables)),
                lastModifiedAt = variableSet.lastModifiedAt,
            )
            UpdateResult(id, true)
        } else UpdateResult(requireNotNull(oldValue.id), false)
    }

    override fun resolve(scope: Scope): VariableSet? = variables[scope]

    override fun get(id: Long): VariableSet? {
        return variables.values.find { it.id == id }
    }

    override fun resolve(scopes: List<Scope>, id: QualifiedId): ResolvedVariable {
        return scopes.asSequence()
            .mapNotNull { s ->
                variables[s]?.variables?.get(id)
                    ?.let { ResolvedVariable(s, id, it) }
            }
            .firstOrNull()
            ?: throw NoSuchElementException("Unknown variable $id in scopes $scopes.")
    }

    override fun toString(): String {
        return "DirectVariableResolver(variables=$variables)"
    }
}
