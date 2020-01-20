package org.radarbase.appconfig.service

import com.nhaarman.mockitokotlin2.mock
import nl.thehyve.lang.expression.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.appconfig.config.AuthenticationConfig
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.SingleVariable
import org.radarbase.appconfig.inject.ClientInterpreter
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.inject.InMemoryResourceEnhancer
import org.radarbase.appconfig.managementportal.MPClient
import org.radarbase.jersey.auth.Auth
import java.net.URL

internal class ProjectServiceTest {
    private lateinit var service: MPProjectService
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
        val conditionService = ConditionService(resolver, ClientInterpreter(resolver))
        val clientService = ClientService(mpClient)
        service = MPProjectService(mpClient, resolver, conditionService, clientService)
    }

    @Test
    fun projectConfig() {
        resolver["aRMT"].register("project.radar-test", "a.c", "b".toVariable())
        resolver["aRMT"].register("project.radar-test", "a.d", 5.toVariable())
        println(resolver["aRMT"])
        val config = service.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", listOf(
                SingleVariable("a.c", "b", "project.radar-test"),
                SingleVariable("a.d", "5", "project.radar-test"))), config)
        val configEmpty = service.projectConfig("aRMT", "radar-demo")
        assertEquals(configEmpty, ClientConfig("aRMT", listOf()))
    }

    @Test
    fun putConfig() {
        val configEmpty = service.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", listOf()), configEmpty)
        service.putProjectConfig("aRMT", "radar-test", ClientConfig("aRMT", listOf(
                        SingleVariable("c", "b"),
                        SingleVariable("d", "5"))
        ))

        val config = service.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", listOf(
                        SingleVariable("c", "b", "project.radar-test"),
                        SingleVariable("d", "5", "project.radar-test"))), config)

        service.putProjectConfig("aRMT", "radar-test", ClientConfig("aRMT", listOf(
                        SingleVariable("c", "b")
        )))
        val configNew = service.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", listOf(
                                SingleVariable("c", "b", "project.radar-test")
                )), configNew)

        service.putProjectConfig("aRMT", "radar-test", ClientConfig("aRMT", listOf(
                        SingleVariable("c", null)
                )))
        val configNull = service.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", listOf(
                        SingleVariable("c", null, "project.radar-test")
                )), configNull)
    }


    @Test
    fun putUserConfig() {
        val configEmpty = service.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", listOf()), configEmpty)
        service.putUserConfig("aRMT", "a", ClientConfig("aRMT", listOf(
                SingleVariable("c", "b"),
                SingleVariable("d", "5"))
        ))

        val config = service.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", listOf(
                SingleVariable("c", "b", "user.a"),
                SingleVariable("d", "5", "user.a"))), config)

        service.putUserConfig("aRMT", "a", ClientConfig("aRMT", listOf(
                SingleVariable("c", "b")
        )))
        val configNew = service.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", listOf(
                SingleVariable("c", "b", "user.a")
        )), configNew)

        service.putProjectConfig("aRMT", "radar-test", ClientConfig("aRMT", listOf(
                SingleVariable("d", "else")
        )))
        val configNull = service.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", listOf(
                SingleVariable("c", "b", "user.a"),
                SingleVariable("d", "else", "project.radar-test")
        )), configNull)
    }
}
