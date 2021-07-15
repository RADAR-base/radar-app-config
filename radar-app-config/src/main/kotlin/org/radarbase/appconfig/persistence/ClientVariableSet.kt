package org.radarbase.appconfig.persistence

import nl.thehyve.lang.expression.VariableSet

data class ClientVariableSet(
    val clientId: String,
    val variableSet: VariableSet,
)
