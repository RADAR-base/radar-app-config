package org.radarbase.appconfig.config

import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope

sealed class AppConfigScope(override val id: QualifiedId) : Scope {
    override fun splitHead(): Pair<String?, AppConfigScope?> {
        val (head, tailId) = id.splitHead()
        return head to tailId?.let { Scopes.parse(it) }
    }

    override fun plus(part: String): AppConfigScope {
        return Scopes.parse(id + part)
    }

    override fun isPrefixedBy(prefix: String): Boolean {
        return id.isPrefixedBy(prefix)
    }

    override fun prefixWith(prefix: String): Scope {
        return Scopes.parse(id.prefixWith(prefix))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Scope) return false

        return id == other.id
    }
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = id.asString()
}

class DynamicScope(val subScope: AppConfigScope) : AppConfigScope(QualifiedId("\$D") + subScope.id)
class ConditionScope(val name: String, val project: ProjectScope) : AppConfigScope(QualifiedId("\$c", name) + project.id) {
    constructor(name: String, projectId: String) : this(name, ProjectScope(projectId))
}
class GlobalScope : AppConfigScope(QualifiedId("\$g"))
class ProjectScope(val projectId: String) : AppConfigScope(QualifiedId("\$p", projectId))
class UserScope(val userId: String, val projectScope: ProjectScope? = null) : AppConfigScope(if (projectScope != null) QualifiedId("\$u", userId) + projectScope.id else QualifiedId("\$u", userId)) {
    constructor(userId: String, projectId: String) : this(userId, ProjectScope(projectId))
}
class ConfigScope(val subScope: Scope) : AppConfigScope(QualifiedId("\$C") + subScope.id)
class ProtocolScope(val subScope: Scope) : AppConfigScope(QualifiedId("\$P") + subScope.id)

object Scopes {
    val AppConfigScope.dynamic: DynamicScope
        get() = DynamicScope(this)
    val AppConfigScope.config: ConfigScope
        get() = ConfigScope(this)
    val AppConfigScope.protocol: ProtocolScope
        get() = ProtocolScope(this)

    fun Scope.toAppConfigScope(): AppConfigScope = parse(id)
    fun String.toAppConfigScope(): AppConfigScope = parse(this)
    fun String.toQualifiedId(): QualifiedId = QualifiedId(this)
    fun Array<String>.toQualifiedId(): QualifiedId = QualifiedId(*this)

    val GLOBAL_SCOPE = GlobalScope()
    val GLOBAL_CONFIG_SCOPE = GLOBAL_SCOPE.config
    val GLOBAL_PROTOCOL_SCOPE = GLOBAL_SCOPE.protocol
    val GLOBAL_DYNAMIC_SCOPE = GLOBAL_SCOPE.dynamic

    fun parse(idString: String): AppConfigScope = parse(QualifiedId(idString))

    fun parse(id: QualifiedId): AppConfigScope {
        val (token, tail) = id.splitHead()
        return when (token) {
            null -> throw IllegalArgumentException("Cannot parse empty ID")
            "\$D" -> DynamicScope(parse(requireNotNull(tail) { "Cannot make dynamic scope without sub scope" }))
            "\$C" -> ConfigScope(parse(requireNotNull(tail) { "Cannot make config scope without sub scope" }))
            "\$c" -> {
                requireNotNull(tail) { "Condition scope needs a condition name." }
                val (conditionName, conditionTail) = tail.splitHead()
                requireNotNull(conditionName) { "Condition scope needs a condition name." }
                val projectScope = conditionTail?.let { parse(it) as? ProjectScope }
                ConditionScope(conditionName, requireNotNull(projectScope) { "Condition scope needs a project scope" })
            }
            "\$P" -> ProtocolScope(parse(requireNotNull(tail) { "Cannot make config scope without sub scope" }))
            "\$g" -> {
                require(tail == null) { "Global scope does not take any arguments" }
                GlobalScope()
            }
            "\$u" -> {
                requireNotNull(tail) { "User scope needs a user ID." }
                val (userId, userTail) = tail.splitHead()
                requireNotNull(userId) { "User needs a user ID." }
                if (userTail == null) {
                    UserScope(userId, null)
                } else {
                    val projectScope = parse(userTail) as? ProjectScope
                    UserScope(
                        userId = userId,
                        projectScope = requireNotNull(projectScope) { "If user has more scope elements, it must contain a project." },
                    )
                }
            }
            "\$p" -> {
                require(tail != null && tail.size == 1) { "Project scope needs a project ID" }
                ProjectScope(tail[0])
            }
            else -> throw IllegalArgumentException("Cannot parse scope $id")
        }
    }
}
