package org.radarbase.appconfig.persistence

import org.radarbase.lang.expression.*

interface ConfigRepository {
    fun update(
        clientId: String,
        variableSet: VariableSet,
    ): UpdateResult

    fun findActiveValue(
        clientId: String,
        scopes: List<Scope>,
        id: QualifiedId,
    ): ResolvedVariable?

    fun findActive(
        clientId: String,
        scope: Scope,
    ): VariableSet?

    fun get(
        id: Long,
    ): ClientVariableSet?
}
