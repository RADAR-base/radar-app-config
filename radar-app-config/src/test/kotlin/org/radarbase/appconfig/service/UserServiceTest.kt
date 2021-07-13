package org.radarbase.appconfig.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.SingleVariable
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.inject.InMemoryResourceEnhancer

internal class UserServiceTest {
    private lateinit var userService: UserService
    private lateinit var projectService: ConfigProjectService
    private lateinit var resolver: ClientVariableResolver

    @BeforeEach
    fun setUp() {
        resolver = InMemoryResourceEnhancer.InMemoryClientVariableResolver()
        userService = UserService(resolver)
        projectService = ConfigProjectServiceImpl(resolver)
    }

    @Test
    fun putUserConfig() {
        val configEmpty = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", "user.a", listOf()), configEmpty)
        userService.putUserConfig(
            "aRMT", "a", ClientConfig(
                null, null, listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5")
                )
            )
        )

        val config = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(
            ClientConfig(
                "aRMT", "user.a", listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5")
                )
            ), config
        )

        userService.putUserConfig(
            "aRMT", "a", ClientConfig(
                null, null, listOf(
                    SingleVariable("c", "b")
                )
            )
        )
        val configNew = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(
            ClientConfig(
                "aRMT", "user.a", listOf(
                    SingleVariable("c", "b")
                )
            ), configNew
        )

        projectService.putProjectConfig(
            "aRMT", "radar-test", ClientConfig(
                null, null, listOf(
                    SingleVariable("d", "else")
                )
            )
        )
        val configNull = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(
            ClientConfig(
                "aRMT", "user.a", listOf(
                    SingleVariable("c", "b")
                ),
                listOf(
                    SingleVariable("d", "else", "project.radar-test")
                )
            ), configNull
        )
    }
}
