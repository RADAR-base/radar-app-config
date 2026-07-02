package org.radarbase.appconfig.persistence

import org.radarbase.appconfig.persistence.entity.ConfigEntity
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.Scope

interface VariableRepository {
    suspend fun query(
        scopes: Collection<Scope>? = null,
        id: QualifiedId? = null,
        prefix: QualifiedId? = null,
        version: Int? = null,
    ): Sequence<ConfigEntity>
}
