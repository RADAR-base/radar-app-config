package org.radarbase.appconfig.config

import org.radarbase.appconfig.config.Scopes.CONDITION_TOKEN
import org.radarbase.appconfig.config.Scopes.CONFIG_TOKEN
import org.radarbase.appconfig.config.Scopes.DYNAMIC_TOKEN
import org.radarbase.appconfig.config.Scopes.GLOBAL_TOKEN
import org.radarbase.appconfig.config.Scopes.PROJECT_TOKEN
import org.radarbase.appconfig.config.Scopes.PROTOCOL_TOKEN
import org.radarbase.appconfig.config.Scopes.USER_TOKEN
import org.radarbase.appconfig.config.Scopes.toQualifiedId
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope

private val dynamicId = DYNAMIC_TOKEN.toQualifiedId()
private val configId = CONFIG_TOKEN.toQualifiedId()
private val protocolId = PROTOCOL_TOKEN.toQualifiedId()
private val globalId = GLOBAL_TOKEN.toQualifiedId()

sealed class AppConfigScope(override val id: QualifiedId) : Scope {
    open val subScope: AppConfigScope? = null
    open fun scopes(): Sequence<AppConfigScope> = sequence {
        yield(this@AppConfigScope)
        subScope?.let { yieldAll(it.scopes()) }
    }

    override fun plus(part: String): AppConfigScope = Scopes.parse(id + part)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Scope) return false

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = id.asString()
}

class DynamicScope(override val subScope: AppConfigScope) : AppConfigScope(dynamicId + subScope.id)

class ConditionScope(val name: String, val project: ProjectScope) : AppConfigScope(QualifiedId(CONDITION_TOKEN, name) + project.id) {
    constructor(name: String, projectId: String) : this(name, ProjectScope(projectId))

    override val subScope: AppConfigScope = project

    companion object {
        fun conditionScopeString(name: String, projectId: String) = QualifiedId(CONDITION_TOKEN, name, PROJECT_TOKEN, projectId).asString()
    }
}

object GlobalScope : AppConfigScope(globalId)

class ProjectScope(val projectId: String) : AppConfigScope(QualifiedId(PROJECT_TOKEN, projectId))

class UserScope(val userId: String, val projectScope: ProjectScope? = null) : AppConfigScope(
    if (projectScope != null) QualifiedId(USER_TOKEN, userId) + projectScope.id
    else QualifiedId(USER_TOKEN, userId)
) {
    constructor(userId: String, projectId: String) : this(userId, ProjectScope(projectId))

    override val subScope: AppConfigScope? = projectScope
}

class ConfigScope(override val subScope: AppConfigScope) : AppConfigScope(configId + subScope.id)

class ProtocolScope(override val subScope: AppConfigScope) : AppConfigScope(protocolId + subScope.id)

