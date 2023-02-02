package org.radarbase.appconfig.config

import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope

object Scopes {
    val AppConfigScope.dynamic: DynamicScope
        get() = DynamicScope(this)
    val AppConfigScope.config: ConfigScope
        get() = ConfigScope(this)
    val AppConfigScope.protocol: ProtocolScope
        get() = ProtocolScope(this)

    fun Scope.toAppConfigScope(): AppConfigScope = if (this is AppConfigScope) this else parse(id)
    fun String.toAppConfigScope(): AppConfigScope = parse(QualifiedId(this))
    fun String.toQualifiedId(): QualifiedId = QualifiedId(this)

    val GLOBAL_CONFIG_SCOPE = GlobalScope.config
    val GLOBAL_PROTOCOL_SCOPE = GlobalScope.protocol
    val GLOBAL_DYNAMIC_SCOPE = GlobalScope.dynamic

    fun parse(id: QualifiedId): AppConfigScope = when (id.head()) {
        DYNAMIC_TOKEN -> DynamicScope(parse(id.tail()))
        CONFIG_TOKEN -> ConfigScope(parse(id.tail()))
        CONDITION_TOKEN -> {
            val condition = id.tail()
            val projectScope = parse(condition.tail())
            require(projectScope is ProjectScope) { "Condition scope needs a project scope" }
            ConditionScope(condition.head(), projectScope)
        }
        PROTOCOL_TOKEN -> ProtocolScope(parse(id.tail()))
        GLOBAL_TOKEN -> {
            require(id.size == 1) { "Global scope does not take any arguments" }
            GlobalScope
        }
        USER_TOKEN -> {
            val user = id.tail()
            val userId = user.head()
            if (user.size > 1) {
                val projectScope = parse(user.tail())
                require(projectScope is ProjectScope) { "User scope needs a project scope" }
                UserScope(userId, projectScope)
            } else {
                UserScope(userId, null)
            }
        }
        PROJECT_TOKEN -> {
            require(id.size == 2) { "Project scope needs a project ID" }
            ProjectScope(id.names[1])
        }
        else -> throw IllegalArgumentException("Cannot parse scope $id")
    }

    internal const val DYNAMIC_TOKEN = "\$D"
    internal const val CONFIG_TOKEN = "\$C"
    internal const val PROTOCOL_TOKEN = "\$P"
    internal const val GLOBAL_TOKEN = "\$g"
    internal const val PROJECT_TOKEN = "\$p"
    internal const val USER_TOKEN = "\$u"
    const val CONDITION_TOKEN = "\$c"
}
