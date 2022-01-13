package org.radarbase.appconfig.service

import org.radarbase.lang.expression.register
import org.radarbase.lang.expression.toVariable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.SingleVariable
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.inject.InMemoryResourceEnhancer

internal class ProjectServiceTest {
    private lateinit var projectService: ConfigProjectService
    private lateinit var resolver: ClientVariableResolver

    @BeforeEach
    fun setUp() {
        resolver = InMemoryResourceEnhancer.InMemoryClientVariableResolver()
        projectService = ConfigProjectServiceImpl(resolver)
    }

    @Test
    fun projectConfig() {
        resolver["aRMT"].register("project.radar-test", "a.c", "b".toVariable())
        resolver["aRMT"].register("project.radar-test", "a.d", 5.toVariable())
        println(resolver["aRMT"])
        assertEquals(
            ClientConfig(
                "aRMT", "project.radar-test", listOf(
                    SingleVariable("a.c", "b"),
                    SingleVariable("a.d", "5")
                )
            ),
            projectService.projectConfig("aRMT", "radar-test")
        )

        resolver["aRMT"].register("global", "a.c", "f".toVariable())
        resolver["aRMT"].register("global", "a.e", 5.toVariable())

        assertEquals(
            ClientConfig(
                "aRMT", "project.radar-test",
                listOf(
                    SingleVariable("a.c", "b"),
                    SingleVariable("a.d", "5")
                ),
                listOf(
                    SingleVariable("a.c", "f", "global"),
                    SingleVariable("a.e", "5", "global")
                )
            ),
            projectService.projectConfig("aRMT", "radar-test")
        )

        assertEquals(
            ClientConfig(
                "aRMT", "project.radar-demo", listOf(), listOf(
                    SingleVariable("a.c", "f", "global"),
                    SingleVariable("a.e", "5", "global")
                )
            ),
            projectService.projectConfig("aRMT", "radar-demo")
        )
    }

    @Test
    fun putProjectConfig() {
        val configEmpty = projectService.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", "project.radar-test", listOf()), configEmpty)
        projectService.putProjectConfig(
            "aRMT", "radar-test", ClientConfig(
                null, null, listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5")
                )
            )
        )

        val config = projectService.projectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                "aRMT", "project.radar-test", listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5")
                )
            ), config
        )

        projectService.putProjectConfig(
            "aRMT", "radar-test", ClientConfig(
                null, null, listOf(
                    SingleVariable("c", "b")
                )
            )
        )
        val configNew = projectService.projectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                "aRMT", "project.radar-test", listOf(
                    SingleVariable("c", "b")
                )
            ), configNew
        )

        projectService.putProjectConfig(
            "aRMT", "radar-test", ClientConfig(
                null, null, listOf(
                    SingleVariable("c", null)
                )
            )
        )
        val configNull = projectService.projectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                "aRMT", "project.radar-test", listOf(
                    SingleVariable("c", null)
                )
            ), configNull
        )
    }
}
