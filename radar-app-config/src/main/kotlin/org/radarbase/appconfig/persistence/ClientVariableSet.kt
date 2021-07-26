package org.radarbase.appconfig.persistence

import org.radarbase.lang.expression.VariableSet

data class ClientVariableSet(
    val clientId: String,
    val variableSet: VariableSet,
)
