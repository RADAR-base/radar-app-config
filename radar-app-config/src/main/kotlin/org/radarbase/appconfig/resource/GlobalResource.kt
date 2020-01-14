package org.radarbase.appconfig.resource

import org.radarbase.appconfig.domain.GlobalConfig
import org.radarbase.appconfig.service.ConfigService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarcns.auth.authorization.Permission
import javax.inject.Singleton
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("global")
@Singleton
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class GlobalResource(
        @Context private val configService: ConfigService
) {
    @PUT
    @Path("config")
    @NeedsPermission(Permission.Entity.PROJECT, Permission.Operation.CREATE)
    fun updateConfig(config: GlobalConfig): Response {
        configService.putConfig(config)
        return Response.noContent().build()
    }

    @Path("config")
    @GET
    fun globalConfig(): GlobalConfig = configService.globalConfig()
}
