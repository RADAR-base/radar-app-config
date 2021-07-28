package org.radarbase.appconfig.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersionDetector
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriBuilder
import org.radarbase.appconfig.config.*
import org.radarbase.appconfig.config.Scopes.GLOBAL_PROTOCOL_SCOPE
import org.radarbase.appconfig.config.Scopes.protocol
import org.radarbase.appconfig.domain.ClientProtocol
import org.radarbase.appconfig.domain.ProtocolMapper
import org.radarbase.appconfig.persistence.ConfigRepository
import org.radarbase.auth.authorization.Permission.PROJECT_READ
import org.radarbase.auth.authorization.Permission.SUBJECT_READ
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.exception.HttpBadRequestException
import org.radarbase.jersey.exception.HttpInvalidContentException
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.managementportal.RadarProjectService
import org.radarbase.lang.expression.Scope
import org.radarbase.lang.expression.UpdateResult
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class ProtocolService(
    @Context private val projectService: RadarProjectService,
    @Context private val configRepository: ConfigRepository,
    @Context private val protocolMapper: ProtocolMapper,
    @Context private val clientService: ClientService,
    @Context private val mapper: ObjectMapper,
    @Context private val conditionService: ConditionService,
) {
    private val schemaValidators: ConcurrentMap<String, Optional<JsonSchema>> = ConcurrentHashMap()

    fun globalProtocol(clientId: String): ClientProtocol {
        val scopes = listOf(GLOBAL_PROTOCOL_SCOPE)
        return firstProtocol(clientId, scopes) { "Client $clientId global protocol not found" }
    }

    fun setGlobalProtocol(clientProtocol: ClientProtocol): UpdateResult =
        updateProtocol(clientProtocol, GLOBAL_PROTOCOL_SCOPE)

    fun getProtocol(protocolId: Long, auth: Auth): ClientProtocol {
        val protocol = configRepository.get(protocolId)
            ?: throw HttpNotFoundException("protocol_not_found", "Protocol $protocolId not found")
        val (clientId, variableSet) = protocol
        val (type, scope) = variableSet.scope.splitHead()
        if (type != "protocol") {
            throw HttpNotFoundException("protocol_not_found", "Protocol $protocolId not found")
        }

        when {
            scope == null -> Unit
            scope.isPrefixedBy("global") -> Unit
            scope.isPrefixedBy("project") -> {
                val projectId = scope.id.names[1]
                projectService.ensureProject(projectId)
                auth.checkPermissionOnProject(PROJECT_READ, projectId, "getProtocol")
            }
            scope.isPrefixedBy("user") -> {
                val userId = scope.id.names[1]
                val user = projectService.userProjects(auth, SUBJECT_READ)
                    .firstNotNullOfOrNull { project -> projectService.getUser(project.id, userId) }
                    ?: throw HttpNotFoundException("user_not_found", "User $userId not found.")
                auth.checkPermissionOnSubject(SUBJECT_READ, user.projectId, user.id)
            }
        }

        clientService.ensureClient(clientId)
        return protocolMapper.protocolToDto(protocol.clientId, variableSet)
    }

    fun projectProtocol(clientId: String, projectId: String): ClientProtocol {
        val scopes = listOf(ProjectScope(projectId).protocol, GLOBAL_PROTOCOL_SCOPE)
        return firstProtocol(clientId, scopes) { "Client $clientId protocol not found for project $projectId" }
    }

    fun setProjectProtocol(
        clientProtocol: ClientProtocol,
        projectId: String
    ): UpdateResult = updateProtocol(clientProtocol, ProjectScope(projectId))

    fun userProtocol(clientId: String, projectId: String, userId: String): ClientProtocol {
        val scopes = mutableListOf(UserScope(userId).protocol)
        scopes += conditionService.matchingScopes(clientId, projectId, userId).map { it.protocol }
        scopes += ProjectScope(projectId).protocol
        scopes += GLOBAL_PROTOCOL_SCOPE

        return firstProtocol(clientId, scopes) { "Client $clientId protocol not found for user $userId in project $projectId" }
    }

    fun conditionProtocol(clientId: String, projectId: String, conditionName: String): ClientProtocol {
        val projectScope = ProjectScope(projectId)
        val scopes = listOf(
            ConditionScope(conditionName, projectScope).protocol,
            projectScope.protocol,
            GLOBAL_PROTOCOL_SCOPE,
        )

        return firstProtocol(clientId, scopes) { "Client $clientId protocol not found for condition $conditionName in project $projectId" }
    }

    fun setConditionProtocol(
        clientProtocol: ClientProtocol,
        projectId: String,
        conditionName: String
    ): UpdateResult = updateProtocol(clientProtocol, ConditionScope(conditionName, projectId))

    fun setUserProtocol(
        clientProtocol: ClientProtocol,
        userId: String
    ): UpdateResult = updateProtocol(clientProtocol, UserScope(userId))

    private fun updateProtocol(clientProtocol: ClientProtocol, scope: AppConfigScope): UpdateResult {
        verifyProtocol(clientProtocol)
        val variableSet = protocolMapper.dtoToProtocol(
            clientProtocol.copy(
                scope = (if (scope is ProtocolScope) scope else scope.protocol).asString(),
                lastModifiedAt = Instant.now(),
            )
        )
        return configRepository.update(clientId = clientProtocol.clientId, variableSet)
    }

    private inline fun firstProtocol(clientId: String, scopes: List<Scope>, exceptionMessageProvider: () -> String): ClientProtocol {
        return protocolMapper.protocolToDto(
            clientId,
            scopes.firstNotNullOfOrNull { configRepository.findActive(clientId, it) }
                ?: throw HttpNotFoundException("protocol_not_found", exceptionMessageProvider()),
        )
    }

    private fun verifyProtocol(protocol: ClientProtocol) {
        val validator = schemaValidators.computeIfAbsent(protocol.clientId) { clientId ->
            try {
                val schema = ProtocolService::class.java
                    .getResourceAsStream("protocol-schema-$clientId.json")
                    ?.use {
                        mapper.readTree(it)
                    }
                if (schema == null) {
                    logger.error("Cannot retrieve protocol schema. Will not verify protocols.")
                    Optional.empty()
                } else {
                    val jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schema))
                    Optional.of(
                        jsonSchemaFactory.getSchema(schema)
                            .also { it.initializeValidators() }
                    )
                }
            } catch (ex: Exception) {
                Optional.empty()
            }
        }.orElseThrow {
            HttpBadRequestException("protocol_not_allowed", "Protocol not allowed for client ${protocol.clientId}")
        }

        val validation = validator.validateAndCollect(protocol.contents).validationMessages
        if (validation.isNotEmpty()) {
            val message = if (validation.size > 5) {
                val messages = validation.asSequence()
                    .take(5)
                    .joinToString(separator = "\n") { message -> "  - [${message.code}] ${message.message}" }
                "$messages\n  - ... ${validation.size - 5} more message(s) ..."
            } else {
                validation.joinToString(separator = "\n") { message -> "  - [${message.code}] ${message.message}" }
            }
            logger.error("Uploaded protocol for client {} does not match schema:\n{}", protocol.clientId, message)
            throw HttpInvalidContentException("Protocol does not match schema:\n$message")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ProtocolService::class.java)

        fun UpdateResult.toResponse(baseUriBuilder: UriBuilder): Response {
            val newLocation = baseUriBuilder.path("protocols/protocol/$id").build()
            return if (didUpdate) {
                Response.created(newLocation)
            } else {
                Response.notModified().header("Location", newLocation)
            }.build()
        }
    }
}
