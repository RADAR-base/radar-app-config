package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.radarbase.appconfig.Config
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.GlobalConfig
import org.radarbase.appconfig.domain.SingleVariable
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.appconfig.managementportal.MockAuth
import java.net.URL

internal class ProjectServiceTest {
    private lateinit var service: ProjectService
    private lateinit var resolver: VariableResolver

    @BeforeEach
    fun setUp() {
        resolver = DirectVariableResolver()
        val config = Config().apply {
            managementPortalUrl = URL("https://radar-test.thehyve.net/managementportal/")
            clientSecret = "appconfig_test"
        }
        val mpClient = MPClient(config, MockAuth())
        val conditionService = ConditionService(resolver, Interpreter(resolver))
        service = ProjectService(mpClient, resolver, conditionService)
    }

    @Test
    fun projectConfig() {
        resolver.register("project.radar-test", "a.c", "b".toVariable())
        resolver.register("project.radar-test", "a.d", 5.toVariable())
        val config = service.projectConfig("radar-test")
        assertEquals(config, GlobalConfig(clients = mapOf("a" to
                ClientConfig("a", listOf(
                        SingleVariable("c", "b", "project.radar-test"),
                        SingleVariable("d", "5", "project.radar-test")))
        )))
        val configEmpty = service.projectConfig("radar-demo")
        assertEquals(configEmpty, GlobalConfig(mapOf()))
    }

    @Test
    fun putConfig() {
        val configEmpty = service.projectConfig("radar-test")
        assertEquals(configEmpty, GlobalConfig(mapOf()))
        service.putConfig("radar-test", GlobalConfig(clients = mapOf("a" to
                ClientConfig("a", listOf(
                        SingleVariable("c", "b"),
                        SingleVariable("d", "5"))
        ))))

        val config = service.projectConfig("radar-test")
        assertEquals(config, GlobalConfig(clients = mapOf("a" to
                ClientConfig("a", listOf(
                        SingleVariable("c", "b", "project.radar-test"),
                        SingleVariable("d", "5", "project.radar-test")))
        )))

        service.putConfig("radar-test", GlobalConfig(clients = mapOf("a" to
                ClientConfig("a", listOf(
                        SingleVariable("c", "b")
        )))))
        val configNew = service.projectConfig("radar-test")
        assertEquals(configNew, GlobalConfig(clients = mapOf("a" to
                ClientConfig("a", listOf(
                                SingleVariable("c", "b", "project.radar-test")
                )))))

        service.putConfig("radar-test", GlobalConfig(clients = mapOf("a" to
                ClientConfig("a", listOf(
                        SingleVariable("c", null)
                )))))
        val configNull = service.projectConfig("radar-test")
        assertEquals(configNull, GlobalConfig(clients = mapOf("a" to
                ClientConfig("a", listOf(
                        SingleVariable("c", null, "project.radar-test")
                )))))
    }
}