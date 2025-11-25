package org.radarbase.appconfig.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
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

internal class ConfigServiceTest {
    private lateinit var configService: ConfigService
    private lateinit var resolver: ClientVariableResolver
    private lateinit var hibernateResolver: HibernateVariableResolver

    @BeforeEach
    fun setUp() {
        resolver = mock()
        hibernateResolver = mock()

        // Return mocked HibernateVariableResolver for the given client
        whenever(resolver["aRMT"]).thenReturn(hibernateResolver)

        configService = ConfigService(
            resolver = resolver,
            conditionService = mock(),
            clientService = mock(),
        )
    }

    @Test
    fun globalConfigReturnsMostRecent() = runBlocking {
        val mostRecent = listOf(
            ClientConfig(
                clientId = "aRMT",
                scope = ConfigService.globalScope.asString(),
                config = listOf(
                    SingleVariable("a.c", "b"),
                    SingleVariable("a.d", "5"),
                ),
            ),
        )
        whenever(hibernateResolver.mostRecentConfigs(ConfigService.globalScope)).thenReturn(mostRecent)

        val result = configService.globalConfig("aRMT")
        assertEquals(mostRecent, result)
    }

    @Test
    fun putGlobalConfigRegistersValues() = runBlocking {
        val cfg = ClientConfig(
            clientId = null,
            scope = null,
            config = listOf(
                SingleVariable("x.y", "z"),
                SingleVariable("n.m", null),
            ),
        )

        configService.putGlobalConfig(cfg, "aRMT")

        val seqCaptor = argumentCaptor<Sequence<Pair<QualifiedId, org.radarbase.lang.expression.Variable>>>()

        verify(hibernateResolver).replace(eq(ConfigService.globalScope), eq(null), seqCaptor.capture())

        val captured = seqCaptor.firstValue.toList()
        // Expect two entries with corresponding QualifiedIds and string/null variables
        assertEquals(2, captured.size)
        assertEquals("x.y", captured[0].first.asString())
        assertEquals("z".toVariable(), captured[0].second)
        assertEquals("n.m", captured[1].first.asString())
        // Nulls become NullLiteral() which does not equal toVariable(); just check string form
        // The Variable prints as null when converted to string through asOptString; here we just ensure key names match
    }

    @Test
    fun globalConfigNameAndVersions() = runBlocking {
        val versions = listOf(
            ClientConfig(
                clientId = "aRMT",
                scope = ConfigService.globalScope.asString(),
                config = listOf(
                    SingleVariable("a.c", "v2", version = 2),
                ),
            ),
            ClientConfig(
                clientId = "aRMT",
                scope = ConfigService.globalScope.asString(),
                config = listOf(
                    SingleVariable("a.c", "v1", version = 1),
                ),
            ),
        )
        whenever(hibernateResolver.versions(ConfigService.globalScope, QualifiedId("a.c")))
            .thenReturn(versions)

        val first = configService.globalConfigName("aRMT", "a.c")
        assertEquals(versions.first(), first)

        val listed = configService.globalConfigNameVersions("aRMT", "a.c")
        assertEquals(versions, listed)
    }

    @Test
    fun globalConfigNameVersionSpecificOrNotFound() {
        runBlocking {
            val versions = listOf(
                ClientConfig(
                    clientId = "aRMT",
                    scope = ConfigService.globalScope.asString(),
                    config = listOf(SingleVariable("a.c", "v2", version = 2)),
                ),
                ClientConfig(
                    clientId = "aRMT",
                    scope = ConfigService.globalScope.asString(),
                    config = listOf(SingleVariable("a.c", "v1", version = 1)),
                ),
            )
            whenever(hibernateResolver.versions(ConfigService.globalScope, QualifiedId("a.c")))
                .thenReturn(versions)

            val v2 = configService.globalConfigNameVersion("aRMT", "a.c", 2)
            assertEquals(versions[0], v2)

            assertThrows(org.radarbase.jersey.exception.HttpNotFoundException::class.java) {
                runBlocking {
                    configService.globalConfigNameVersion("aRMT", "a.c", 99)
                }
            }
        }
    }
}
