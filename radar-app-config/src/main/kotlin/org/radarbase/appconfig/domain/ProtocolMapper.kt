package org.radarbase.appconfig.domain

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.ws.rs.core.Context
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.SimpleScope
import org.radarbase.lang.expression.VariableSet
import org.radarbase.lang.expression.toVariable

class ProtocolMapper(
    @Context private val mapper: ObjectMapper,
) {
    fun protocolToDto(clientId: String, protocol: VariableSet): ClientProtocol {
        val contents = protocol.variables[QualifiedId("contents")]?.asOptString()
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
            scope = SimpleScope(requireNotNull(protocol.scope)),
            variables = mapOf(
                QualifiedId("contents") to mapper.writeValueAsString(protocol.contents).toVariable()
            ),
            lastModifiedAt = protocol.lastModifiedAt,
        )
    }
}