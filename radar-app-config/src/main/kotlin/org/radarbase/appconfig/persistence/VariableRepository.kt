package org.radarbase.appconfig.persistence

import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.ResolvedVariable
import org.radarbase.lang.expression.Scope

interface VariableRepository {
    suspend fun query(
        scopes: Collection<Scope>? = null,
        id: QualifiedId? = null,
        prefix: QualifiedId? = null,
        version: Int? = null
    ): Sequence<ResolvedVariable>
}
