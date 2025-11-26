package org.radarbase.appconfig.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.api.SingleVariable
import org.radarbase.appconfig.inject.ClientInterpreter
import org.radarbase.appconfig.inject.ClientVariableResolver
import org.radarbase.appconfig.inject.InMemoryResourceEnhancer
import org.radarbase.lang.expression.toVariable
import org.radarbase.lang.expression.register

internal class ConfigServiceTest {
    private lateinit var configService: ConfigService
    private lateinit var resolver: ClientVariableResolver

    @BeforeEach
    fun setUp() {
        resolver = InMemoryResourceEnhancer.InMemoryClientVariableResolver()
        configService = ConfigService(
            resolver = resolver,
            conditionService = ConditionService(resolver, ClientInterpreter(resolver)),
            clientService = mock(),
        )
    }

    @Test
    fun globalConfigReturnsMostRecent() = runBlocking {
        resolver["aRMT"].register("global", "a.c", "b".toVariable())
        resolver["aRMT"].register("global", "a.d", 5.toVariable())

        val result = configService.globalConfig("aRMT")
        assertEquals(
            ClientConfig(
                "aRMT",
                ConfigService.globalScope.asString(),
                listOf(
                    SingleVariable("a.c", "b", "global", "aRMT", null, null, null),
                    SingleVariable("a.d", "5", "global", "aRMT", null, null, null),
                ),
            ),
            result,
        )
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

        val result = configService.globalConfig("aRMT")
        assertEquals(
            ClientConfig(
                "aRMT",
                ConfigService.globalScope.asString(),
                listOf(
                    SingleVariable("x.y", "z", "global", "aRMT", null, null, null),
                    SingleVariable("n.m", null, "global", "aRMT", null, null, null),
                ),
            ),
            result,
        )
    }

    @Test
    fun globalConfigNameAndVersions() = runBlocking {
        resolver["aRMT"].register("global", "a.c", "v2".toVariable())

        val first = configService.globalConfigName("aRMT", "a.c")
        assertEquals(
            ClientConfig(
                "aRMT",
                ConfigService.globalScope.asString(),
                listOf(SingleVariable("a.c", "v2", "global", "aRMT", null, null, null)),
                emptyList(),
            ),
            first,
        )

        val listed = configService.globalConfigNameVersions("aRMT", "a.c")
        assertEquals(
            listOf(
                ClientConfig(
                    "aRMT",
                    ConfigService.globalScope.asString(),
                    listOf(SingleVariable("a.c", "v2", "global", "aRMT", null, null, null)),
                    null,
                ),
            ),
            listed,
        )
    }

    @Test
    fun globalConfigNameVersionSpecific() = runBlocking {
        resolver["aRMT"].register("global", "a.c", "v2".toVariable())

        val v2 = configService.globalConfigNameVersion("aRMT", "a.c", 2)
        assertEquals(
            ClientConfig(
                "aRMT",
                ConfigService.globalScope.asString(),
                listOf(SingleVariable("a.c", "v2", "global", "aRMT", null, null, null)),
                null,
            ),
            v2,
        )
    }
}
