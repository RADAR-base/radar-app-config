package org.radarbase.appconfig.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.SingleVariable
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.inject.InMemoryResourceEnhancer
import org.radarbase.lang.expression.register
import org.radarbase.lang.expression.toVariable

internal class ProjectServiceTest {
    private lateinit var projectService: ConfigProjectService
    private lateinit var resolver: ClientVariableResolver

    @BeforeEach
    fun setUp() {
        resolver = InMemoryResourceEnhancer.InMemoryClientVariableResolver()
        projectService = ConfigProjectServiceImpl(resolver)
    }

    @Test
    fun projectConfig() = runBlocking {
        resolver["aRMT"].register("project.radar-test", "a.c", "b".toVariable())
        resolver["aRMT"].register("project.radar-test", "a.d", 5.toVariable())
        println(resolver["aRMT"])
        assertEquals(
            ClientConfig(
                "aRMT",
                "project.radar-test",
                listOf(
                    SingleVariable("a.c", "b", "project.radar-test", "aRMT", null, null, null),
                    SingleVariable("a.d", "5", "project.radar-test", "aRMT", null, null, null),
                ),
            ),
            projectService.getProjectConfig("aRMT", "radar-test"),
        )

        resolver["aRMT"].register("global", "a.c", "f".toVariable())
        resolver["aRMT"].register("global", "a.e", 5.toVariable())

        assertEquals(
            ClientConfig(
                "aRMT",
                "project.radar-test",
                listOf(
                    SingleVariable("a.c", "b", "project.radar-test", "aRMT", null, null, null),
                    SingleVariable("a.d", "5", "project.radar-test", "aRMT", null, null, null),
                ),
                listOf(
                    SingleVariable("a.c", "f", "global", "aRMT",  null, null, null),
                    SingleVariable("a.e", "5", "global", "aRMT",  null, null, null),
                ),
            ),
            projectService.getProjectConfig("aRMT", "radar-test"),
        )

        assertEquals(
            ClientConfig(
                "aRMT",
                "project.radar-demo",
                listOf(),
                listOf(
                    SingleVariable("a.c", "f", "global", "aRMT", null, null, null),
                    SingleVariable("a.e", "5", "global", "aRMT", null, null, null),
                ),
            ),
            projectService.getProjectConfig("aRMT", "radar-demo"),
        )
    }

    @Test
    fun putProjectConfig() = runBlocking {
        val configEmpty = projectService.getProjectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", "project.radar-test", listOf()), configEmpty)
        projectService.putProjectConfig(
            "aRMT",
            "radar-test",
            ClientConfig(
                null,
                null,
                listOf(
                    SingleVariable("c", "b", "project.radar-test", "aRMT", null, null, null),
                    SingleVariable("d", "5", "project.radar-test", "aRMT", null, null, null),
                ),
            ),
        )

        val config = projectService.getProjectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                "aRMT",
                "project.radar-test",
                listOf(
                    SingleVariable("c", "b", "project.radar-test", "aRMT", null, null, null),
                    SingleVariable("d", "5", "project.radar-test", "aRMT", null, null, null),
                ),
            ),
            config,
        )

        projectService.putProjectConfig(
            "aRMT",
            "radar-test",
            ClientConfig(
                null,
                null,
                listOf(
                    SingleVariable("c", "b", "project.radar-test", "aRMT", null, null, null),
                ),
            ),
        )
        val configNew = projectService.getProjectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                "aRMT",
                "project.radar-test",
                listOf(
                    SingleVariable("c", "b", "project.radar-test", "aRMT", null, null, null),
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
                    SingleVariable("c", null, "project.radar-test", "aRMT", null, null, null),
                ),
            ),
        )
        val configNull = projectService.getProjectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                "aRMT",
                "project.radar-test",
                listOf(
                    SingleVariable("c", null, "project.radar-test", "aRMT", null, null, null),
                ),
            ),
            configNull,
        )
    }

    @Test
    fun projectConfigNameAndVersions() = runBlocking {
        // Register variables in project and global scopes
        resolver["aRMT"].register("project.radar-test", "p.x", "pv".toVariable())
        resolver["aRMT"].register("global", "p.x", "gv".toVariable())

        // Fetch a specific name (should resolve project scope first)
        val byName = projectService.getProjectConfigByName("radar-test", "aRMT", "p.x")
        assertEquals(
            ClientConfig(
                "aRMT",
                "project.radar-test",
                listOf(
                    // Direct resolver has no metadata or versioning; expect nulls for those fields
                    SingleVariable("p.x", "pv", "project.radar-test", "aRMT", null, null, null),
                ),
                emptyList(),
            ),
            byName,
        )

        // Fetch all versions for the variable name (direct resolver returns single element)
        val versions = projectService.getProjectConfigByNameAndAllVersions("radar-test", "aRMT", "p.x")
        assertEquals(
                ClientConfig(
                    "aRMT",
                    "project.radar-test",
                    listOf(
                        SingleVariable("p.x", "pv", "project.radar-test", "aRMT", null, null, null),
                    ),
                    null,
                ),  versions,
        )

        // Fetch a specific version (the in-memory resolver ignores version and returns the same value)
        val version1 = projectService.getProjectConfigByNameAndVersion("radar-test", "aRMT", "p.x", 1)
        assertEquals(
            ClientConfig(
                "aRMT",
                "project.radar-test",
                listOf(
                    SingleVariable("p.x", "pv", "project.radar-test", "aRMT", null, null, null),
                ),
                null,
            ),
            version1,
        )
    }
}
