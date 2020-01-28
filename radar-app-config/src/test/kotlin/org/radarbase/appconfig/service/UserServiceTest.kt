package org.radarbase.appconfig.service

import com.nhaarman.mockitokotlin2.mock
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

internal class UserServiceTest {
    private lateinit var userService: UserService
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
        val conditionService = ConditionService(resolver, ClientInterpreter(resolver))
        val clientService = ClientService(mpClient)
        userService = UserService(mpClient, conditionService, resolver)
        projectService = MPProjectService(mpClient, resolver)
    }

    @Test
    fun putUserConfig() {
        val configEmpty = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", listOf()), configEmpty)
        userService.putUserConfig("aRMT", "a", ClientConfig("aRMT", listOf(
                SingleVariable("c", "b"),
                SingleVariable("d", "5"))
        ))

        val config = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", listOf(
                SingleVariable("c", "b", "user.a"),
                SingleVariable("d", "5", "user.a"))), config)

        userService.putUserConfig("aRMT", "a", ClientConfig("aRMT", listOf(
                SingleVariable("c", "b")
        )))
        val configNew = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", listOf(
                SingleVariable("c", "b", "user.a")
        )), configNew)

        projectService.putProjectConfig("aRMT", "radar-test", ClientConfig("aRMT", listOf(
                SingleVariable("d", "else")
        )))
        val configNull = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", listOf(
                SingleVariable("c", "b", "user.a"),
                SingleVariable("d", "else", "project.radar-test")
        )), configNull)
    }
}
