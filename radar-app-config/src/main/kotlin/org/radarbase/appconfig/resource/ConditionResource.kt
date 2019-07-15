package org.radarbase.appconfig.resource

import org.radarbase.appconfig.auth.Auth
import org.radarbase.appconfig.auth.Authenticated
import org.radarbase.appconfig.auth.NeedsPermissionOnProject
import org.radarbase.appconfig.domain.Condition
import org.radarbase.appconfig.domain.ConditionList
import org.radarbase.appconfig.exception.HttpApplicationException
import org.radarbase.appconfig.service.ConditionService
import org.radarbase.appconfig.service.ProjectService
import org.radarcns.auth.authorization.Permission
import javax.inject.Singleton
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo

/** Topics submission and listing. Requests need authentication. */
@Path("/projects/{projectId}/conditions")
@Singleton
@Authenticated
@Produces("application/json; charset=utf-8")
@Consumes("application/json")
class ConditionResource(
        @Context private val conditionService: ConditionService,
        @Context private val projectService: ProjectService,
        @Context private val uriInfo: UriInfo
) {
    @POST
    @NeedsPermissionOnProject(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun createCondition(
            @Context auth: Auth,
            @PathParam("projectId") projectId: String,
            condition: Condition
    ): Response {
        if (condition.id != null) {
            throw HttpApplicationException(Response.Status.BAD_REQUEST, "bad_request", "Cannot set condition ID in request.")
        }
        val newCondition = conditionService.create(projectId, condition)
        return Response
                .ok()
                .header("Location", "${uriInfo.path}/${newCondition.id}")
                .build()
    }

    @GET
    @NeedsPermissionOnProject(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    fun listConditions(@PathParam("projectId") projectId: String): ConditionList {
        return ConditionList(conditionService.list(projectId))
    }

    @PUT
    @NeedsPermissionOnProject(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun orderConditions(@PathParam("projectId") projectId: String, conditions: ConditionList): Response {
        conditionService.order(projectId, conditions.conditions)
        return Response.noContent().build()
    }

    @POST
    @Path("{conditionId}")
    @NeedsPermissionOnProject(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun updateCondition(
            @PathParam("projectId") projectId: String,
            @PathParam("conditionId") conditionId: Long,
            condition: Condition
    ) {
        conditionService.update(projectId, condition.copy(id = conditionId))
    }

    @GET
    @Path("{conditionId}")
    @NeedsPermissionOnProject(Permission.Entity.PROJECT, Permission.Operation.READ, "projectId")
    fun condition(
            @PathParam("projectId") projectId: String,
            @PathParam("conditionId") conditionId: Long): Condition {
        return conditionService.get(projectId, conditionId)
    }

    @DELETE
    @Path("{conditionId}")
    @NeedsPermissionOnProject(Permission.Entity.PROJECT, Permission.Operation.UPDATE, "projectId")
    fun deleteCondition(
            @PathParam("projectId") projectId: String,
            @PathParam("conditionId") conditionId: Long): Response {
        conditionService.delete(projectId, conditionId)
        return Response.noContent().build()
    }
}
