package org.radarbase.appconfig.service

import nl.thehyve.lang.expression.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.SingleVariable
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.inject.InMemoryResourceEnhancer
import java.time.Instant

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
        val now = Instant.now()
        val variableSet = VariableSet(
            type = "CONFIG",
            scope = SimpleScope("project.radar-test"),
            variables = mapOf(
                QualifiedId("a.c") to "b".toVariable(),
                QualifiedId("a.d") to 5.toVariable(),
            ),
            lastModifiedAt = now,
        )

        resolver["aRMT"].replace(variableSet)
        println(resolver["aRMT"])
        assertEquals(
            ClientConfig(
                "aRMT",
                "project.radar-test",
                listOf(
                    SingleVariable("a.c", "b"),
                    SingleVariable("a.d", "5"),
                ),
                lastModifiedAt = now,
            ),
            projectService.projectConfig("aRMT", "radar-test")
        )

        val globalVariableSet = VariableSet(
            type = "CONFIG",
            scope = SimpleScope("global"),
            variables = mapOf(
                QualifiedId("a.c") to "f".toVariable(),
                QualifiedId("a.e") to 5.toVariable(),
            ),
            lastModifiedAt = now,
        )
        resolver["aRMT"].replace(globalVariableSet)

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
                ),
                lastModifiedAt = now,
            ),
            projectService.projectConfig("aRMT", "radar-test")
        )

        assertEquals(
            ClientConfig(
                clientId = "aRMT",
                scope = "project.radar-demo",
                config = listOf(),
                defaults = listOf(
                    SingleVariable("a.c", "f", "global"),
                    SingleVariable("a.e", "5", "global"),
                ),
                lastModifiedAt = now,
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
