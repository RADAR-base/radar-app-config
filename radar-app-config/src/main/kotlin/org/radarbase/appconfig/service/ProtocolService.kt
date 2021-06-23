package org.radarbase.appconfig.service

import jakarta.ws.rs.core.Context
import nl.thehyve.lang.expression.Scope
import org.radarbase.appconfig.domain.ClientProtocol
import org.radarbase.appconfig.domain.ProtocolMapper
import org.radarbase.appconfig.persistence.ProtocolRepository
import java.time.Instant

class ProtocolService(
    @Context private val protocolRepository: ProtocolRepository,
    @Context private val protocolMapper: ProtocolMapper,
){
    fun globalProtocol(clientId: String): ClientProtocol {
        val protocol = protocolRepository.protocol(clientId, globalProtocolScope)
        return protocolMapper.protocolToDto(protocol)
    }

    fun setGlobalProtocol(clientProtocol: ClientProtocol): Boolean {
        val updatedProtocol = clientProtocol.copy(
            scope = globalProtocolScope.asString(),
            lastModifiedAt = Instant.now(),
        )
        return protocolRepository.store(updatedProtocol)
    }

    companion object {
        fun protocolScope(scope: Scope) = scope + "protocol"
        val globalProtocolScope = protocolScope(ConfigService.globalScope)
    }
}
