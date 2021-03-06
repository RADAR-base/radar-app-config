package org.radarbase.appconfig.resource

import org.radarbase.appconfig.domain.Condition
import org.radarbase.appconfig.domain.ConditionList
import org.radarbase.appconfig.service.ConditionService
import org.radarbase.appconfig.service.ConfigProjectService
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.exception.HttpBadRequestException
import java.net.URI
import jakarta.inject.Singleton
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo

/** Topics submission and listing. Requests need authentication. */
@Path("/projects/{projectId}/conditions")
@Singleton
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ConditionResource(
    @Context private val conditionService: ConditionService,
    @Context private val projectService: ConfigProjectService,
    @Context private val uriInfo: UriInfo,
) {
    @POST
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun createCondition(
        @PathParam("projectId") projectId: String,
        condition: Condition,
    ): Response {
        if (condition.id != null) {
            throw HttpBadRequestException("bad_request", "Cannot set condition ID in request.")
        }
        val newCondition = conditionService.create(projectId, condition)
        return Response.created(URI.create("${uriInfo.path}/${newCondition.id}"))
            .entity(newCondition)
            .build()
    }

    @GET
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    fun listConditions(@PathParam("projectId") projectId: String): ConditionList {
        return ConditionList(conditionService.list(projectId))
    }

    @PUT
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun orderConditions(@PathParam("projectId") projectId: String, conditions: ConditionList): Response {
        conditionService.order(projectId, conditions.conditions)
        return Response.noContent().build()
    }

    @POST
    @Path("{conditionId}")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun updateCondition(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionId") conditionId: Long,
        condition: Condition,
    ): Condition {
        return conditionService.update(projectId, condition.copy(id = conditionId))
    }

    @GET
    @Path("{conditionId}")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    fun condition(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionId") conditionId: Long,
    ): Condition {
        return conditionService.get(projectId, conditionId)
    }

    @DELETE
    @Path("{conditionId}")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun deleteCondition(
        @PathParam("projectId") projectId: String,
        @PathParam("conditionId") conditionId: Long,
    ): Response {
        conditionService.delete(projectId, conditionId)
        return Response.noContent().build()
    }
}
