package org.radarbase.appconfig.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.SingleVariable
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.persistence.HibernateVariableResolver
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.toVariable

internal class UserServiceTest {
    private lateinit var userService: UserService
    private lateinit var projectService: ConfigProjectService
    private lateinit var resolver: ClientVariableResolver
    private lateinit var hibernateResolver: HibernateVariableResolver

    @BeforeEach
    fun setUp() {
        resolver = mock()
        hibernateResolver = mock()
        whenever(resolver["aRMT"]).thenReturn(hibernateResolver)

        val conditionService = ConditionService(resolver, org.radarbase.appconfig.inject.ClientInterpreter(resolver))
        userService = UserService(conditionService, resolver)
        projectService = ConfigProjectServiceImpl(resolver)
    }

    @Test
    fun putUserConfig() = runBlocking {
        // Start with no configs
        whenever(hibernateResolver.mostRecentConfigs(ConfigService.userScope("a"))).thenReturn(emptyList())
        whenever(hibernateResolver.mostRecentConfigs(ConfigProjectServiceImpl.projectScope("radar-test"))).thenReturn(emptyList())
        whenever(hibernateResolver.mostRecentConfigs(ConfigService.globalScope)).thenReturn(emptyList())

        val configEmpty = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals("aRMT", configEmpty?.clientId)
        assertEquals("user.a", configEmpty?.scope)
        assertEquals(emptyList<SingleVariable>(), configEmpty?.config)
        assertEquals(emptyList<SingleVariable>(), configEmpty?.defaults)

        userService.putUserConfig(
            "aRMT",
            "a",
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
            eq(ConfigService.userScope("a")),
            eq(null),
            seqCaptor.capture(),
        )
        val captured = seqCaptor.firstValue.toList()
        assertEquals(2, captured.size)
        assertEquals("c", captured[0].first.asString())
        assertEquals("b".toVariable(), captured[0].second)
        assertEquals("d", captured[1].first.asString())
        assertEquals("5".toVariable(), captured[1].second)

        // After put, return composed config with user values
        whenever(hibernateResolver.mostRecentConfigs(ConfigService.userScope("a"))).thenReturn(
            listOf(
                ClientConfig(
                    clientId = "aRMT",
                    scope = "user.a",
                    config = listOf(
                        SingleVariable("c", "b"),
                        SingleVariable("d", "5"),
                    ),
                ),
            ),
        )

        val config = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals("aRMT", config?.clientId)
        assertEquals("user.a", config?.scope)
        assertEquals(
            listOf(
                SingleVariable("c", "b"),
                SingleVariable("d", "5"),
            ),
            config?.config,
        )
        assertEquals(emptyList<SingleVariable>(), config?.defaults)

        // Update single key
        userService.putUserConfig(
            "aRMT",
            "a",
            ClientConfig(
                null,
                null,
                listOf(
                    SingleVariable("c", "b"),
                ),
            ),
        )
        verify(hibernateResolver, atLeastOnce()).replace(eq(ConfigService.userScope("a")), eq(null), any())

        // Add project default for d
        whenever(hibernateResolver.mostRecentConfigs(ConfigProjectServiceImpl.projectScope("radar-test"))).thenReturn(
            listOf(
                ClientConfig(
                    clientId = "aRMT",
                    scope = "project.radar-test",
                    config = listOf(
                        SingleVariable("d", "else", "project.radar-test"),
                    ),
                ),
            ),
        )

        val configWithDefaults = userService.userConfig("aRMT", "radar-test", "a")
        assertEquals(
            ClientConfig(
                "aRMT",
                "user.a",
                listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5"),
                ),
                listOf(
                    SingleVariable("d", "else", "project.radar-test"),
                ),
            ),
            configWithDefaults,
        )
    }

    @Test
    fun userConfigNamePrefersUserThenProjectThenGlobal() = runBlocking {
        val name = "a.c"

        // No condition scopes for simplicity
        // Stub versions in user scope: should be returned first
        val userVersions = listOf(
            ClientConfig(
                clientId = "aRMT",
                scope = ConfigService.userScope("a").asString(),
                config = listOf(SingleVariable(name, "u2", version = 2)),
            ),
        )
        whenever(hibernateResolver.versions(ConfigService.userScope("a"), QualifiedId(name))).thenReturn(userVersions)

        val firstUser = userService.userConfigName("aRMT", "radar-test", "a", name)
        assertEquals(userVersions.first(), firstUser)

        // If user scope is empty, fall back to project
        val projectScope = ConfigProjectServiceImpl.projectScope("radar-test")
        whenever(hibernateResolver.versions(ConfigService.userScope("a"), QualifiedId(name))).thenReturn(emptyList())
        val projectVersions = listOf(
            ClientConfig(
                clientId = "aRMT",
                scope = projectScope.asString(),
                config = listOf(SingleVariable(name, "p1", version = 1)),
            ),
        )
        whenever(hibernateResolver.versions(projectScope, QualifiedId(name))).thenReturn(projectVersions)

        val firstProject = userService.userConfigName("aRMT", "radar-test", "a", name)
        assertEquals(projectVersions.first(), firstProject)

        // If project scope also empty, fall back to global
        whenever(hibernateResolver.versions(projectScope, QualifiedId(name))).thenReturn(emptyList())
        val globalVersions = listOf(
            ClientConfig(
                clientId = "aRMT",
                scope = ConfigService.globalScope.asString(),
                config = listOf(SingleVariable(name, "g1", version = 1)),
            ),
        )
        whenever(hibernateResolver.versions(ConfigService.globalScope, QualifiedId(name))).thenReturn(globalVersions)

        val firstGlobal = userService.userConfigName("aRMT", "radar-test", "a", name)
        assertEquals(globalVersions.first(), firstGlobal)
    }

    @Test
    fun userConfigNameVersionsUserScopeOnly() = runBlocking {
        val name = "a.c"
        val userScope = ConfigService.userScope("a")
        val versions = listOf(
            ClientConfig(
                clientId = "aRMT",
                scope = userScope.asString(),
                config = listOf(SingleVariable(name, "u2", version = 2)),
            ),
            ClientConfig(
                clientId = "aRMT",
                scope = userScope.asString(),
                config = listOf(SingleVariable(name, "u1", version = 1)),
            ),
        )
        whenever(hibernateResolver.versions(userScope, QualifiedId(name))).thenReturn(versions)

        val listed = userService.userConfigNameVersions("aRMT", "a", name)
        assertEquals(versions, listed)
    }

    @Test
    fun userConfigNameVersionSpecificOrNotFound() {
        runBlocking {
            val name = "a.c"
            val userScope = ConfigService.userScope("a")
            val versions = listOf(
                ClientConfig(
                    clientId = "aRMT",
                    scope = userScope.asString(),
                    config = listOf(SingleVariable(name, "u2", version = 2)),
                ),
                ClientConfig(
                    clientId = "aRMT",
                    scope = userScope.asString(),
                    config = listOf(SingleVariable(name, "u1", version = 1)),
                ),
            )
            whenever(hibernateResolver.versions(userScope, QualifiedId(name))).thenReturn(versions)

            val v2 = userService.userConfigNameVersion("aRMT", "a", name, 2)
            assertEquals(versions[0], v2)

            assertThrows(org.radarbase.jersey.exception.HttpNotFoundException::class.java) {
                runBlocking {
                    userService.userConfigNameVersion("aRMT", "a", name, 99)
                }
            }
        }
    }
}
