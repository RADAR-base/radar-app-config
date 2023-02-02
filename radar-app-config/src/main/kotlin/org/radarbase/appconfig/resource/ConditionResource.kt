package org.radarbase.appconfig.resource

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.models.annotations.OpenAPI30
import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import java.net.URI
import org.radarbase.appconfig.api.Condition
import org.radarbase.appconfig.api.ConditionList
import org.radarbase.appconfig.api.*
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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
    @Operation(
        description = "List of conditions that are currently available.",
        responses = [
            ApiResponse(description = "List of conditions that are currently available.", responseCode = "200")
        ],
    )
    fun listConditions(@PathParam("projectId") projectId: String): ConditionList {
        return ConditionList(conditionService.list(projectId))
    }

    @POST
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    @Operation(
        description = "Create a new condition",
        responses = [
            ApiResponse(description = "Newly created condition", responseCode = "201"),
            ApiResponse(description = "Condition ID is present or condition name contains illegal characters (# or $)", responseCode = "400"),
        ],
        requestBody = RequestBody(description = "Condition to be created in the current project.", required = true),
    )
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
    @Operation(
        description = "Update an existing condition",
        responses = [
            ApiResponse(description = "Updated condition", responseCode = "200"),
            ApiResponse(description = "Condition ID is present or condition name contains illegal characters (# or $)", responseCode = "400"),
        ],
        requestBody = RequestBody(description = "Condition to be updated in the current project. Only the title, expression and rank will be updated.", required = true),
    )
    fun updateCondition(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
        condition: Condition,
    ): Condition {
        return conditionService.update(projectId, condition.copy(name = conditionName))
    }

    @GET
    @Path("{conditionName}/evaluation")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    @Operation(
        description = "Evaluates how the condition is evaluated for a given user and client.",
        responses = [ApiResponse(description = "Evaluated value", responseCode = "200")],
        parameters = [
            Parameter(name = "user", description = "User ID to evaluate the condition for", required = true),
            Parameter(name = "client", description = "OAuth client to evaluate the condition for", required = true)
        ],
    )
    fun evaluateCondition(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
        @QueryParam("user") userId: String,
        @QueryParam("client") clientId: String,
    ): Evaluation {
        val (condition, evaluation) = conditionService.evaluate(clientId, projectId, conditionName, userId)
        return Evaluation(
            clientId = clientId,
            projectId = projectId,
            userId = userId,
            condition = condition,
            evaluation = evaluation,
        )
    }

    @GET
    @Path("{conditionName}")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    @Operation(
        description = "Get a condition",
        responses = [ApiResponse(description = "Condition", responseCode = "200")],
    )
    fun condition(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionName") conditionName: String,
    ): Condition {
        return conditionService.get(projectId, conditionName)
    }

    @DELETE
    @Path("{conditionName}")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    @Operation(
        description = "Delete a condition",
        responses = [ApiResponse(description = "Condition", responseCode = "204")],
    )
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
    @Operation(
        description = "Update the config for a given condition and client",
        responses = [ApiResponse(description = "Updated configuration", responseCode = "200")],
        requestBody = RequestBody(description = "Configuration to use"),
    )
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
    @Operation(
        description = "Get the config for a given condition and client",
        responses = [ApiResponse(description = "Current configuration", responseCode = "200")],
    )
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
    @Operation(
        description = "Get the protocol for a given condition and client",
        responses = [ApiResponse(description = "Current protocol", responseCode = "200")],
    )
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
    @Operation(
        description = "Get the protocol contents for a given condition and client",
        responses = [ApiResponse(description = "Current protocol contents", responseCode = "200")],
    )
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
    @Operation(
        description = "Create or update a protocol for a given condition and client",
        responses = [
            ApiResponse(description = "Location of the newly created protocol", responseCode = "201", headers = [
                Header(name = "Location", description = "Location of the created protocol"),
            ]),
            ApiResponse(description = "Location of the existing identical protocol", responseCode = "304", headers = [
                Header(name = "Location", description = "Location of the existing protocol"),
            ]),
            ApiResponse(description = "The protocol did not match the schema.", responseCode = "400"),
        ],
        requestBody = RequestBody(description = "A client protocol, including client ID and contents.")
    )
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
    @Operation(
        description = "Update only the protocol contents for a given condition and client",
        responses = [
            ApiResponse(description = "The contents were updated", responseCode = "204"),
            ApiResponse(description = "The protocol did not match the schema.", responseCode = "400"),
        ],
        requestBody = RequestBody(description = "Protocol contents matching the JSON schema.")
    )
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
        return Response.noContent().build()
    }
}
