package org.radarbase.appconfig.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.SingleVariable
import org.radarbase.appconfig.inject.ClientInterpreter
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.inject.InMemoryResourceEnhancer

internal class UserServiceTest {
    private lateinit var userService: UserService
    private lateinit var projectService: ConfigProjectService
    private lateinit var resolver: ClientVariableResolver

    @BeforeEach
    fun setUp() {
        resolver = InMemoryResourceEnhancer.InMemoryClientVariableResolver()

        val conditionService = ConditionService(resolver, ClientInterpreter(resolver))
        userService = UserService(conditionService, resolver)
        projectService = ConfigProjectServiceImpl(resolver)
    }

    @Test
    fun putUserConfig() = runBlocking {
        val configEmpty = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", "user.a", listOf()), configEmpty)
        userService.putUserConfig(
            "aRMT",
            "a",
            ClientConfig(
                null,
                null,
                listOf(
                    SingleVariable("c", "b", "user.a", "aRMT", null, null, null),
                    SingleVariable("d", "5", "user.a", "aRMT", null, null, null),
                ),
            ),
        )

        val config = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(
            ClientConfig(
                "aRMT",
                "user.a",
                listOf(
                    SingleVariable("c", "b", "user.a", "aRMT", null, null, null),
                    SingleVariable("d", "5", "user.a", "aRMT", null, null, null),
                ),
            ),
            config,
        )

        userService.putUserConfig(
            "aRMT",
            "a",
            ClientConfig(
                null,
                null,
                listOf(
                    SingleVariable("c", "b", "user.a", "aRMT", null, null, null),
                ),
            ),
        )
        val configNew = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(
            ClientConfig(
                "aRMT",
                "user.a",
                listOf(
                    SingleVariable("c", "b", "user.a", "aRMT", null, null, null),
                ),
            ),
            configNew,
        )

        projectService.putProjectConfig(
            "aRMT",
            "radar-test",
            ClientConfig(
                null,
                null,
                listOf(
                    SingleVariable("d", "else", "user.a", "aRMT", null, null, null),
                ),
            ),
        )
        val configNull = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(
            ClientConfig(
                "aRMT",
                "user.a",
                listOf(
                    SingleVariable("c", "b", "user.a", "aRMT", null, null, null),
                ),
                listOf(
                    SingleVariable("d", "else", "project.radar-test", "aRMT", null, null, null),
                ),
            ),
            configNull,
        )
    }
}
