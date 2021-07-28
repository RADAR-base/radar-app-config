package org.radarbase.appconfig.domain

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.config.Scopes.toAppConfigScope
import org.radarbase.appconfig.config.Scopes.toQualifiedId
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.lang.expression.VariableSet
import org.radarbase.lang.expression.toVariable

class ProtocolMapper(
    @Context private val mapper: ObjectMapper,
) {
    fun protocolToDto(clientId: String, protocol: VariableSet): ClientProtocol {
        val contents = protocol.variables[contentsId]?.asOptString()
            ?: throw HttpNotFoundException("protocol_not_found", "Protocol definition not found.")
        return ClientProtocol(
            id = requireNotNull(protocol.id),
            clientId = clientId,
            scope = protocol.scope.asString(),
            contents = mapper.readTree(contents),
            lastModifiedAt = protocol.lastModifiedAt,
        )
    }

    fun dtoToProtocol(protocol: ClientProtocol): VariableSet {
        return VariableSet(
            id = null,
            scope = requireNotNull(protocol.scope).toAppConfigScope(),
            variables = mapOf(
                contentsId to mapper.writeValueAsString(protocol.contents).toVariable()
            ),
            lastModifiedAt = protocol.lastModifiedAt,
        )
    }

    companion object {
        private val contentsId = "contents".toQualifiedId()
    }
}
