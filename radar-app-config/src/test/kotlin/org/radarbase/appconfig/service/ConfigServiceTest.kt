package org.radarbase.appconfig.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.appconfig.config.Scopes.toQualifiedId
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.SingleVariable
import org.radarbase.appconfig.inject.InMemoryResourceEnhancer
import org.radarbase.appconfig.persistence.ConfigRepository
import org.radarbase.lang.expression.SimpleScope
import org.radarbase.lang.expression.VariableSet
import org.radarbase.lang.expression.toVariable
import java.time.Instant

internal class ConfigServiceTest {
    private lateinit var configService: ConfigService
    private lateinit var resolver: ConfigRepository

    @BeforeEach
    fun setUp() {
        resolver = InMemoryResourceEnhancer.InMemoryConfigRepository()
        val conditionService = mock<ConditionService> {
            on { matchingScopes(any(), any(), any()) } doReturn emptyList()
        }
        configService = ConfigService(resolver, conditionService)
    }

    @Test
    fun putUserConfig() {
        val now = Instant.now()
        val configEmpty = configService.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", "\$C#\$u#a", listOf()), configEmpty)
        configService.putUserConfig(
            clientId = "aRMT",
            userId = "a",
            clientConfig = ClientConfig(
                clientId = null,
                scope = null,
                config = listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5")
                ),
                lastModifiedAt = now,
            )
        )

        val config = configService.userConfig("aRMT", "radar-test", "a")
        assertEquals(
            ClientConfig(
                clientId = "aRMT",
                scope = "\$C#\$u#a",
                config = listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5")
                ),
                lastModifiedAt = now,
            ),
            config,
        )

        configService.putUserConfig(
            clientId = "aRMT",
            userId = "a",
            clientConfig = ClientConfig(
                clientId = null,
                scope = null,
                config = listOf(
                    SingleVariable("c", "b")
                ),
                lastModifiedAt = now,
            )
        )
        val configNew = configService.userConfig("aRMT", "radar-test", "a")
        assertEquals(
            ClientConfig(
                clientId = "aRMT",
                scope = "\$C#\$u#a",
                config = listOf(
                    SingleVariable("c", "b")
                ),
                lastModifiedAt = now,
            ),
            configNew,
        )

        configService.putProjectConfig(
            clientId = "aRMT",
            projectId = "radar-test",
            clientConfig = ClientConfig(
                clientId = null,
                scope = null,
                config = listOf(
                    SingleVariable("d", "else")
                ),
                lastModifiedAt = now,
            )
        )
        val configNull = configService.userConfig("aRMT", "radar-test", "a")
        assertEquals(
            ClientConfig(
                clientId = "aRMT",
                scope = "\$C#\$u#a",
                config = listOf(
                    SingleVariable("c", "b")
                ),
                defaults = listOf(
                    SingleVariable("d", "else", "\$C#\$p#radar-test")
                ),
                lastModifiedAt = now,
            ),
            configNull,
        )
    }

    @Test
    fun projectConfig() {
        val now = Instant.now()
        val variableSet = VariableSet(
            id = null,
            scope = SimpleScope("\$C#\$p#radar-test"),
            variables = mapOf(
                "a.c".toQualifiedId() to "b".toVariable(),
                "a.d".toQualifiedId() to 5.toVariable(),
            ),
            lastModifiedAt = now,
        )

        resolver.update("aRMT", variableSet)
        println(resolver)
        assertEquals(
            ClientConfig(
                clientId = "aRMT",
                scope = "\$C#\$p#radar-test",
                listOf(
                    SingleVariable("a.c", "b"),
                    SingleVariable("a.d", "5"),
                ),
                lastModifiedAt = now,
            ),
            configService.projectConfig("aRMT", "radar-test")
        )

        val globalVariableSet = VariableSet(
            id = null,
            scope = SimpleScope("\$C#\$g"),
            variables = mapOf(
                "a.c".toQualifiedId() to "f".toVariable(),
                "a.e".toQualifiedId() to 5.toVariable(),
            ),
            lastModifiedAt = now,
        )
        resolver.update("aRMT", globalVariableSet)

        assertEquals(
            ClientConfig(
                "aRMT",
                "\$C#\$p#radar-test",
                listOf(
                    SingleVariable("a.c", "b"),
                    SingleVariable("a.d", "5")
                ),
                listOf(
                    SingleVariable("a.c", "f", "\$C#\$g"),
                    SingleVariable("a.e", "5", "\$C#\$g")
                ),
                lastModifiedAt = now,
            ),
            configService.projectConfig("aRMT", "radar-test")
        )

        assertEquals(
            ClientConfig(
                clientId = "aRMT",
                scope = "\$C#\$p#radar-demo",
                config = listOf(),
                defaults = listOf(
                    SingleVariable("a.c", "f", "\$C#\$g"),
                    SingleVariable("a.e", "5", "\$C#\$g"),
                ),
                lastModifiedAt = now,
            ),
            configService.projectConfig("aRMT", "radar-demo")
        )
    }

    @Test
    fun putProjectConfig() {
        val configEmpty = configService.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", "\$C#\$p#radar-test", listOf()), configEmpty)
        configService.putProjectConfig(
            "aRMT",
            "radar-test",
            ClientConfig(
                clientId = null,
                scope = null,
                config = listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5"),
                ),
            )
        )

        val config = configService.projectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                clientId = "aRMT",
                scope = "\$C#\$p#radar-test",
                config = listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5"),
                ),
            ),
            config,
        )

        configService.putProjectConfig(
            clientId = "aRMT",
            projectId = "radar-test",
            clientConfig = ClientConfig(
                clientId = null,
                scope = null,
                config = listOf(
                    SingleVariable("c", "b"),
                ),
            ),
        )
        val configNew = configService.projectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                clientId = "aRMT",
                scope = "\$C#\$p#radar-test",
                config = listOf(
                    SingleVariable("c", "b"),
                ),
            ),
            configNew,
        )

        configService.putProjectConfig(
            clientId = "aRMT",
            projectId = "radar-test",
            ClientConfig(
                clientId = null,
                scope = null,
                listOf(
                    SingleVariable("c", null),
                ),
            ),
        )
        val configNull = configService.projectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                clientId = "aRMT",
                scope = "\$C#\$p#radar-test",
                config = listOf(
                    SingleVariable("c", null),
                ),
            ),
            configNull,
        )
    }
}
