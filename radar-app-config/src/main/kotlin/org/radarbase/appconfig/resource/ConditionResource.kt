package org.radarbase.appconfig.resource

import com.fasterxml.jackson.databind.JsonNode
import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.ClientProtocol
import org.radarbase.appconfig.domain.Condition
import org.radarbase.appconfig.domain.ConditionList
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.ConditionService
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.appconfig.service.ProtocolService
import org.radarbase.appconfig.service.ProtocolService.Companion.toResponse
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.cache.Cache
import org.radarbase.jersey.exception.HttpBadRequestException
import java.net.URI

/** Topics submission and listing. Requests need authentication. */
@Path("/projects/{projectId}/conditions")
@Singleton
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ConditionResource(
    @Context private val conditionService: ConditionService,
    @Context private val uriInfo: UriInfo,
    @Context private val clientService: ClientService,
    @Context private val configService: ConfigService,
    @Context private val protocolService: ProtocolService,
) {
    @GET
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    fun listConditions(@PathParam("projectId") projectId: String): ConditionList {
        return ConditionList(conditionService.list(projectId))
    }

    @POST
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun createCondition(
        @PathParam("projectId") projectId: String,
        condition: Condition,
    ): Response {
        if (condition.id != null) {
            throw HttpBadRequestException("bad_request", "Cannot set condition ID in request.")
        }
        if (condition.name.contains('#') || condition.name.contains('$')) {
            throw HttpBadRequestException("bad_condition_name", "Condition name cannot contain # or $ characters.")
        }
        val newCondition = conditionService.create(projectId, condition)
        return Response.created(URI.create("${uriInfo.path}/${newCondition.id}"))
            .entity(newCondition)
            .build()
    }

    @POST
    @Path("{conditionName}")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun updateCondition(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
        condition: Condition,
    ): Condition {
        return conditionService.update(projectId, condition.copy(name = conditionName))
    }

    @GET
    @Path("{conditionName}")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    fun condition(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
    ): Condition {
        return conditionService.get(projectId, conditionName)
    }

    @DELETE
    @Path("{conditionName}")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun deleteCondition(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
    ): Response {
        conditionService.deactivate(projectId, conditionName)
        return Response.noContent().build()
    }

    @POST
    @Path("{conditionName}/config/{clientId}")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun updateConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
        @PathParam("clientId") clientId: String,
        config: ClientConfig,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        configService.setConditionConfig(clientId, projectId, conditionName, config)
        return configService.conditionConfig(clientId, projectId, conditionName)
    }

    @Path("{conditionName}/config/{clientId}")
    @GET
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    fun getConfig(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
        @PathParam("clientId") clientId: String,
    ): ClientConfig {
        clientService.ensureClient(clientId)
        return configService.conditionConfig(clientId, projectId, conditionName)
    }

    @Path("{conditionName}/protocols/{clientId}")
    @GET
    @Cache(maxAge = 300, isPrivate = true)
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    fun protocol(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
        @PathParam("clientId") clientId: String,
    ): ClientProtocol {
        clientService.ensureClient(clientId)
        return protocolService.conditionProtocol(clientId, projectId, conditionName)
    }

    @Path("{conditionName}/protocols/{clientId}/contents")
    @GET
    @Cache(maxAge = 300, isPrivate = true)
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    fun protocolContents(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
        @PathParam("clientId") clientId: String,
    ): JsonNode {
        clientService.ensureClient(clientId)
        return protocolService.conditionProtocol(clientId, projectId, conditionName).contents
    }

    @Path("{conditionName}/protocols")
    @POST
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun setProtocol(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
        @Context uriInfo: UriInfo,
        clientProtocol: ClientProtocol,
    ): Response {
        clientService.ensureClient(clientProtocol.clientId)
        val updateResult = protocolService.setConditionProtocol(clientProtocol, projectId, conditionName)
        return updateResult.toResponse(uriInfo.baseUriBuilder)
    }

    @Path("{conditionName}/protocols/{clientId}/contents")
    @PUT
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun setProtocol(
        @PathParam("projectId") projectId: String,
        @PathParam("clientId") clientId: String,
        @PathParam("conditionName") conditionName: String,
        protocol: JsonNode,
    ): Response {
        clientService.ensureClient(clientId)
        protocolService.setConditionProtocol(
            ClientProtocol(
                clientId = clientId,
                contents = protocol,
            ),
            projectId,
            conditionName,
        )
        return Response.notModified().build()
    }
}
