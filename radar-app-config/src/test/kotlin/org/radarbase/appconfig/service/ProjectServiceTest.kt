package org.radarbase.appconfig.service

import com.nhaarman.mockitokotlin2.mock
import nl.thehyve.lang.expression.register
import nl.thehyve.lang.expression.toVariable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.appconfig.config.AuthenticationConfig
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.SingleVariable
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.inject.InMemoryResourceEnhancer
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.jersey.auth.Auth
import java.net.URL

internal class ProjectServiceTest {
    private lateinit var projectService: ConfigProjectService
    private lateinit var resolver: ClientVariableResolver

    @BeforeEach
    fun setUp() {
        resolver = InMemoryResourceEnhancer.InMemoryClientVariableResolver()
        val config = ApplicationConfig(
                authentication = AuthenticationConfig(
                        url = URL("https://radar-test.thehyve.net/managementportal/"),
                        clientSecret = "peyman"))

        val auth = mock<Auth> {}
        val mpClient = MPClient(config, auth)
        projectService = MPProjectService(mpClient, resolver)
    }

    @Test
    fun projectConfig() {
        resolver["aRMT"].register("project.radar-test", "a.c", "b".toVariable())
        resolver["aRMT"].register("project.radar-test", "a.d", 5.toVariable())
        println(resolver["aRMT"])
        assertEquals(
                ClientConfig("aRMT", listOf(
                        SingleVariable("a.c", "b", "project.radar-test"),
                        SingleVariable("a.d", "5", "project.radar-test"))),
                projectService.projectConfig("aRMT", "radar-test"))

        resolver["aRMT"].register("global", "a.c", "f".toVariable())
        resolver["aRMT"].register("global", "a.e", 5.toVariable())

        assertEquals(
                ClientConfig("aRMT", listOf(
                        SingleVariable("a.c", "b", "project.radar-test"),
                        SingleVariable("a.d", "5", "project.radar-test"),
                        SingleVariable("a.e", "5", "global"))),
                projectService.projectConfig("aRMT", "radar-test"))

        assertEquals(
                ClientConfig("aRMT", listOf(
                        SingleVariable("a.c", "f", "global"),
                        SingleVariable("a.e", "5", "global"))),
                projectService.projectConfig("aRMT", "radar-demo"))
    }

    @Test
    fun putProjectConfig() {
        val configEmpty = projectService.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", listOf()), configEmpty)
        projectService.putProjectConfig("aRMT", "radar-test", ClientConfig("aRMT", listOf(
                        SingleVariable("c", "b"),
                        SingleVariable("d", "5"))
        ))

        val config = projectService.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", listOf(
                        SingleVariable("c", "b", "project.radar-test"),
                        SingleVariable("d", "5", "project.radar-test"))), config)

        projectService.putProjectConfig("aRMT", "radar-test", ClientConfig("aRMT", listOf(
                        SingleVariable("c", "b")
        )))
        val configNew = projectService.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", listOf(
                                SingleVariable("c", "b", "project.radar-test")
                )), configNew)

        projectService.putProjectConfig("aRMT", "radar-test", ClientConfig("aRMT", listOf(
                        SingleVariable("c", null)
                )))
        val configNull = projectService.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", listOf(
                        SingleVariable("c", null, "project.radar-test")
                )), configNull)
    }
}
