package org.radarbase.appconfig.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.SingleVariable
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.persistence.HibernateVariableResolver
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.toVariable

internal class ProjectServiceTest {
    private lateinit var projectService: ConfigProjectService
    private lateinit var resolver: ClientVariableResolver
    private lateinit var hibernateResolver: HibernateVariableResolver

    @BeforeEach
    fun setUp() {
        resolver = mock()
        hibernateResolver = mock()
        whenever(resolver["aRMT"]).thenReturn(hibernateResolver)
        projectService = ConfigProjectServiceImpl(resolver)
    }

    @Test
    fun projectConfig() = runBlocking {
        // Stub project scope most recent
        whenever(hibernateResolver.mostRecentConfigs(ConfigProjectServiceImpl.projectScope("radar-test")))
            .thenReturn(
                listOf(
                    ClientConfig(
                        clientId = "aRMT",
                        scope = "project.radar-test",
                        config = listOf(
                            SingleVariable("a.c", "b"),
                            SingleVariable("a.d", "5"),
                        ),
                    ),
                ),
            )
        // Stub global defaults
        whenever(hibernateResolver.mostRecentConfigs(ConfigService.globalScope))
            .thenReturn(
                listOf(
                    ClientConfig(
                        clientId = "aRMT",
                        scope = ConfigService.globalScope.asString(),
                        config = listOf(
                            SingleVariable("a.c", "f", "global"),
                            SingleVariable("a.e", "5", "global"),
                        ),
                    ),
                ),
            )

        val cfg = projectService.projectConfig("aRMT", "radar-test")
        // Compare fields individually to avoid strict data class equality differences
        assertEquals("aRMT", cfg?.clientId)
        assertEquals("project.radar-test", cfg?.scope)
        assertEquals(
            listOf(
                SingleVariable("a.c", "b"),
                SingleVariable("a.d", "5"),
            ),
            cfg?.config,
        )
        assertEquals(
            listOf(
                SingleVariable("a.c", "f", "global"),
                SingleVariable("a.e", "5", "global"),
            ),
            cfg?.defaults,
        )

        // For another project, no project configs, only global defaults
        whenever(hibernateResolver.mostRecentConfigs(ConfigProjectServiceImpl.projectScope("radar-demo")))
            .thenReturn(emptyList())

        val cfg2 = projectService.projectConfig("aRMT", "radar-demo")
        assertEquals("aRMT", cfg2?.clientId)
        assertEquals("project.radar-demo", cfg2?.scope)
        assertEquals(emptyList<SingleVariable>(), cfg2?.config)
        assertEquals(
            listOf(
                SingleVariable("a.c", "f", "global"),
                SingleVariable("a.e", "5", "global"),
            ),
            cfg2?.defaults,
        )
    }

    @Test
    fun putProjectConfig() = runBlocking {
        // Ensure projectConfig works by stubbing empty lists
        whenever(hibernateResolver.mostRecentConfigs(ConfigProjectServiceImpl.projectScope("radar-test")))
            .thenReturn(emptyList())
        whenever(hibernateResolver.mostRecentConfigs(ConfigService.globalScope))
            .thenReturn(emptyList())

        val configEmpty = projectService.projectConfig("aRMT", "radar-test")
        assertEquals("aRMT", configEmpty?.clientId)
        assertEquals("project.radar-test", configEmpty?.scope)
        assertEquals(emptyList<SingleVariable>(), configEmpty?.config)
        assertEquals(emptyList<SingleVariable>(), configEmpty?.defaults)

        projectService.putProjectConfig(
            "aRMT",
            "radar-test",
            ClientConfig(
                null,
                null,
                listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5"),
                ),
            ),
        )

        val seqCaptor = argumentCaptor<Sequence<Pair<QualifiedId, org.radarbase.lang.expression.Variable>>>()
        verify(hibernateResolver, atLeastOnce()).replace(
            eq(ConfigProjectServiceImpl.projectScope("radar-test")),
            eq(null),
            seqCaptor.capture(),
        )

        val captured1 = seqCaptor.firstValue.toList()
        assertEquals(2, captured1.size)
        assertEquals("c", captured1[0].first.asString())
        assertEquals("b".toVariable(), captured1[0].second)
        assertEquals("d", captured1[1].first.asString())
        assertEquals("5".toVariable(), captured1[1].second)

        // Update with single key
        projectService.putProjectConfig(
            "aRMT",
            "radar-test",
            ClientConfig(
                null,
                null,
                listOf(SingleVariable("c", "b")),
            ),
        )
        verify(hibernateResolver, atLeastOnce()).replace(
            eq(ConfigProjectServiceImpl.projectScope("radar-test")),
            eq(null),
            any(),
        )

        // Set null value
        projectService.putProjectConfig(
            "aRMT",
            "radar-test",
            ClientConfig(
                null,
                null,
                listOf(SingleVariable("c", null)),
            ),
        )
        verify(hibernateResolver, atLeastOnce()).replace(
            eq(ConfigProjectServiceImpl.projectScope("radar-test")),
            eq(null),
            any(),
        )
    }

    @Test
    fun projectConfigName_fallbackToGlobal() = runBlocking {
        val name = "a.c"
        val projectScope = ConfigProjectServiceImpl.projectScope("radar-test")
        val versionsProject = listOf(
            ClientConfig(
                clientId = "aRMT",
                scope = projectScope.asString(),
                config = listOf(SingleVariable(name, "p2", version = 2)),
            ),
            ClientConfig(
                clientId = "aRMT",
                scope = projectScope.asString(),
                config = listOf(SingleVariable(name, "p1", version = 1)),
            ),
        )
        val versionsGlobal = listOf(
            ClientConfig(
                clientId = "aRMT",
                scope = ConfigService.globalScope.asString(),
                config = listOf(SingleVariable(name, "g2", version = 2)),
            ),
        )

        // Case 1: project has versions, should return first of project
        whenever(hibernateResolver.versions(projectScope, QualifiedId(name))).thenReturn(versionsProject)
        whenever(hibernateResolver.versions(ConfigService.globalScope, QualifiedId(name))).thenReturn(versionsGlobal)

        val firstProject = projectService.projectConfigName("radar-test", "aRMT", name)
        assertEquals(versionsProject.first(), firstProject)

        // Case 2: project empty, fall back to global first
        whenever(hibernateResolver.versions(projectScope, QualifiedId(name))).thenReturn(emptyList())
        val firstGlobal = projectService.projectConfigName("radar-test", "aRMT", name)
        assertEquals(versionsGlobal.first(), firstGlobal)
    }

    @Test
    fun projectConfigNameVersionsListProjectOnly() = runBlocking {
        val name = "a.c"
        val projectScope = ConfigProjectServiceImpl.projectScope("radar-test")
        val versionsProject = listOf(
            ClientConfig(
                clientId = "aRMT",
                scope = projectScope.asString(),
                config = listOf(SingleVariable(name, "p2", version = 2)),
            ),
            ClientConfig(
                clientId = "aRMT",
                scope = projectScope.asString(),
                config = listOf(SingleVariable(name, "p1", version = 1)),
            ),
        )
        whenever(hibernateResolver.versions(projectScope, QualifiedId(name))).thenReturn(versionsProject)

        val listed = projectService.projectConfigNameVersions("radar-test", "aRMT", name)
        assertEquals(versionsProject, listed)
    }

    @Test
    fun projectConfigNameVersionSpecificOrNotFound() {
        runBlocking {
            val name = "a.c"
            val projectScope = ConfigProjectServiceImpl.projectScope("radar-test")
            val versionsProject = listOf(
                ClientConfig(
                    clientId = "aRMT",
                    scope = projectScope.asString(),
                    config = listOf(SingleVariable(name, "p2", version = 2)),
                ),
                ClientConfig(
                    clientId = "aRMT",
                    scope = projectScope.asString(),
                    config = listOf(SingleVariable(name, "p1", version = 1)),
                ),
            )
            whenever(hibernateResolver.versions(projectScope, QualifiedId(name))).thenReturn(versionsProject)

            val v2 = projectService.projectConfigNameVersion("radar-test", "aRMT", name, 2)
            assertEquals(versionsProject[0], v2)

            assertThrows(org.radarbase.jersey.exception.HttpNotFoundException::class.java) {
                runBlocking {
                    projectService.projectConfigNameVersion("radar-test", "aRMT", name, 99)
                }
            }
        }
    }
}
