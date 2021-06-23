package org.radarbase.appconfig.domain

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.ws.rs.core.Context
import org.radarbase.appconfig.persistence.entity.ProtocolEntity

class ProtocolMapper(
    @Context private val mapper: ObjectMapper,
) {
    fun protocolToDto(protocolEntity: ProtocolEntity): ClientProtocol {
        return ClientProtocol(
            clientId = protocolEntity.clientId,
            scope = protocolEntity.scope,
            contents = mapper.readTree(protocolEntity.contents),
        )
    }
}
