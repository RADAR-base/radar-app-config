package org.radarbase.appconfig.config

import org.radarbase.appconfig.config.Scopes.toQualifiedId
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope

private const val DYNAMIC_TOKEN = "\$D"
private const val CONFIG_TOKEN = "\$C"
private const val PROTOCOL_TOKEN = "\$P"
private const val GLOBAL_TOKEN = "\$g"
private const val PROJECT_TOKEN = "\$p"
private const val USER_TOKEN = "\$u"
const val CONDITION_TOKEN = "\$c"

private val DYNAMIC_ID = DYNAMIC_TOKEN.toQualifiedId()
private val CONFIG_ID = CONFIG_TOKEN.toQualifiedId()
private val PROTOCOL_ID = PROTOCOL_TOKEN.toQualifiedId()
private val GLOBAL_ID = GLOBAL_TOKEN.toQualifiedId()

sealed class AppConfigScope(override val id: QualifiedId) : Scope {
    override fun splitHead(): Pair<String?, AppConfigScope?> {
        val (head, tailId) = id.splitHead()
        return head to tailId?.let { Scopes.parse(it) }
    }

    override fun plus(part: String): AppConfigScope = Scopes.parse(id + part)

    override fun isPrefixedBy(prefix: String): Boolean = id.isPrefixedBy(prefix)

    override fun prefixWith(prefix: String): Scope = Scopes.parse(id.prefixWith(prefix))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Scope) return false

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = id.asString()
}

class DynamicScope(val subScope: AppConfigScope) : AppConfigScope(DYNAMIC_ID + subScope.id)

class ConditionScope(val name: String, val project: ProjectScope) : AppConfigScope(QualifiedId(CONDITION_TOKEN, name) + project.id) {
    constructor(name: String, projectId: String) : this(name, ProjectScope(projectId))

    companion object {
        fun conditionScopeString(name: String, projectId: String) = QualifiedId(CONDITION_TOKEN, name, PROJECT_TOKEN, projectId).asString()
    }
}

class GlobalScope : AppConfigScope(GLOBAL_ID)

class ProjectScope(val projectId: String) : AppConfigScope(QualifiedId(PROJECT_TOKEN, projectId))

class UserScope(val userId: String, val projectScope: ProjectScope? = null) : AppConfigScope(
    if (projectScope != null) QualifiedId(USER_TOKEN, userId) + projectScope.id
    else QualifiedId(USER_TOKEN, userId)
) {
    constructor(userId: String, projectId: String) : this(userId, ProjectScope(projectId))
}

class ConfigScope(val subScope: Scope) : AppConfigScope(CONFIG_ID + subScope.id)

class ProtocolScope(val subScope: Scope) : AppConfigScope(PROTOCOL_ID + subScope.id)

object Scopes {
    val AppConfigScope.dynamic: DynamicScope
        get() = DynamicScope(this)
    val AppConfigScope.config: ConfigScope
        get() = ConfigScope(this)
    val AppConfigScope.protocol: ProtocolScope
        get() = ProtocolScope(this)

    fun Scope.toAppConfigScope(): AppConfigScope = if (this is AppConfigScope) this else parse(id)
    fun String.toAppConfigScope(): AppConfigScope = parse(this)
    fun String.toQualifiedId(): QualifiedId = QualifiedId(this)

    val GLOBAL_SCOPE = GlobalScope()
    val GLOBAL_CONFIG_SCOPE = GLOBAL_SCOPE.config
    val GLOBAL_PROTOCOL_SCOPE = GLOBAL_SCOPE.protocol
    val GLOBAL_DYNAMIC_SCOPE = GLOBAL_SCOPE.dynamic

    fun parse(idString: String): AppConfigScope = parse(QualifiedId(idString))

    fun parse(id: QualifiedId): AppConfigScope {
        val (token, tail) = id.splitHead()
        return when (token) {
            null -> throw IllegalArgumentException("Cannot parse empty ID")
            DYNAMIC_TOKEN -> DynamicScope(parse(requireNotNull(tail) { "Cannot make dynamic scope without sub scope" }))
            CONFIG_TOKEN -> ConfigScope(parse(requireNotNull(tail) { "Cannot make config scope without sub scope" }))
            CONDITION_TOKEN -> {
                requireNotNull(tail) { "Condition scope needs a condition name." }
                val (conditionName, conditionTail) = tail.splitHead()
                requireNotNull(conditionName) { "Condition scope needs a condition name." }
                val projectScope = requireNotNull(conditionTail?.let { parse(it) as? ProjectScope }) { "Condition scope needs a project scope" }
                ConditionScope(conditionName, projectScope)
            }
            PROTOCOL_TOKEN -> ProtocolScope(parse(requireNotNull(tail) { "Cannot make config scope without sub scope" }))
            GLOBAL_TOKEN -> {
                require(tail == null) { "Global scope does not take any arguments" }
                GlobalScope()
            }
            USER_TOKEN -> {
                requireNotNull(tail) { "User scope needs a user ID." }
                val (userId, userTail) = tail.splitHead()
                requireNotNull(userId) { "User needs a user ID." }
                if (userTail != null) {
                    val projectScope = requireNotNull(parse(userTail) as? ProjectScope) { "If user has more scope elements, it must contain a project." }
                    UserScope(userId, projectScope)
                } else {
                    UserScope(userId, null)
                }
            }
            PROJECT_TOKEN -> {
                require(tail != null && tail.size == 1) { "Project scope needs a project ID" }
                ProjectScope(tail[0])
            }
            else -> throw IllegalArgumentException("Cannot parse scope $id")
        }
    }
}
