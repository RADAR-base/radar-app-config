package nl.thehyve.lang.expression

import me.xdrop.fuzzywuzzy.FuzzySearch
import java.math.BigDecimal
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

interface VariableResolver {
    fun register(scope: Scope, id: QualifiedId, variable: Variable)
    fun resolve(scope: Scope, id: QualifiedId): Variable
    fun list(scope: Scope, prefix: QualifiedId?): Stream<QualifiedId>
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
    override fun list(scope: Scope, prefix: QualifiedId?): Stream<QualifiedId> {
        val usePrefix = prefix?.names ?: listOf()
        var refStream = root.variableNames(scope)
                .distinct()
                .map { QualifiedId(it) }

        if (usePrefix.isNotEmpty()) {
            refStream = refStream
                    .filter { it.names.count() >= usePrefix.count()
                            && it.names.subList(0, usePrefix.count()) == usePrefix }
        }

        return refStream
    }

    val root = ScopeNode.root()

    override fun register(scope: Scope, id: QualifiedId, variable: Variable) {
        val scopeNode = root.ensure(scope)
        val variables = scopeNode.variables
                ?: mutableMapOf<String, Variable>()
                        .also { scopeNode.variables = it }

        variables[id.asString()] = variable
    }

    override fun resolve(scope: Scope, id: QualifiedId): Variable {
        val result = root.resolve(scope, id)
        if (result == null) {
            val variables = list(scope, null)
                    .collect(Collectors.toList())

            val query = id.asString().replace('.', ' ')

            val alternatives = FuzzySearch.extractSorted(query, variables, { it.names.joinToString(" ") }, 75)
                    .take(5)

            if (alternatives.isEmpty()) {
                throw UnsupportedOperationException("Unknown variable $id in scope ${scope.id}.")
            } else {
                throw UnsupportedOperationException("Unknown variable $id in scope ${scope.id}." +
                        " Did you mean any of the following variables?\n - ${alternatives.joinToString(separator="\n - ") { it.referent.asString() }}")
            }
        } else {
            return result
        }
    }
}

data class ScopeNode(val name: String?, val parent: ScopeNode?, val children: MutableList<ScopeNode>, var variables: MutableMap<String, Variable>?) {
    operator fun get(name: String): ScopeNode? = children.find { it.name == name }

    fun ensure(scope: Scope): ScopeNode {
        val (childName, innerScope) = scope.splitHead() ?: return this

        val child = this[childName]
                ?: ScopeNode(childName, this, mutableListOf(), null)
                        .also { children.add(it) }

        return child.ensure(innerScope)
    }

    fun resolve(scope: Scope, id: QualifiedId): Variable? = scope.splitHead()
            ?.let { (childName, innerScope) ->
                this[childName]?.resolve(innerScope, id)
            }
            ?: variables?.get(id.asString())

    fun resolveDirect(scope: Scope, id: QualifiedId): Variable? {
        val (childName, innerScope) = scope.splitHead() ?: return variables?.get(id.asString())
        return this[childName]?.resolveDirect(innerScope, id)
    }

    fun variableNames(scope: Scope): Stream<String> {
        val childStream = scope.splitHead()?.let { (childName, innerScope) ->
            this[childName]?.variableNames(innerScope)
        } ?: Stream.empty()
        val thisStream = variables?.keys?.stream() ?: Stream.empty()
        return Stream.concat(childStream, thisStream)
    }

    override fun toString() = buildString(50) {
        if (parent == null) {
            append("Scope[ ")
        }
        name?.let { append(it).append(": ") }

        if (children.isNotEmpty()) {
            append(children)
            variables?.let { append(", ").append(it).append(')')}
        } else {
            variables?.let { append(it).append(')')}
        }
        if (parent == null) {
            append(" ]")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScopeNode

        return name == other.name
                && children == other.children
                && variables == other.variables
    }

    override fun hashCode() = Objects.hash(name, children)

    companion object {
        fun root() = ScopeNode(null, null, mutableListOf(), null)
    }
}
