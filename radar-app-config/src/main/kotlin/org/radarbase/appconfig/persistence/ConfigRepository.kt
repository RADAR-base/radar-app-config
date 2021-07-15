package org.radarbase.appconfig.persistence

import nl.thehyve.lang.expression.Scope
import nl.thehyve.lang.expression.UpdateResult
import nl.thehyve.lang.expression.VariableSet

interface ConfigRepository {
    fun update(
        clientId: String,
        variableSet: VariableSet,
    ): UpdateResult

    fun findActive(
        clientId: String,
        scope: Scope,
    ): VariableSet?

    fun get(
        id: Long,
    ): ClientVariableSet?
}
