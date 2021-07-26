package org.radarbase.appconfig.service

import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.SimpleScope
import org.radarbase.lang.expression.VariableSet
import org.radarbase.lang.expression.toVariable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.appconfig.domain.ClientConfig
import org.radarbase.appconfig.domain.SingleVariable
import org.radarbase.appconfig.inject.InMemoryResourceEnhancer
import org.radarbase.appconfig.persistence.ConfigRepository
import java.time.Instant

internal class ConfigServiceTest {
    private lateinit var configService: ConfigService
    private lateinit var resolver: ConfigRepository

    @BeforeEach
    fun setUp() {
        resolver = InMemoryResourceEnhancer.InMemoryConfigRepository()
        configService = ConfigService(resolver)
    }

    @Test
    fun putUserConfig() {
        val now = Instant.now()
        val configEmpty = configService.userConfig("aRMT", "radar-test", "a")
        assertEquals(ClientConfig("aRMT", "config.user.a", listOf()), configEmpty)
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
                scope = "config.user.a",
                config = listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5")
                ),
                lastModifiedAt = now,
            ), config
        )

        configService.putUserConfig(
            "aRMT", "a", ClientConfig(
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
                scope = "config.user.a",
                config = listOf(
                    SingleVariable("c", "b")
                ),
                lastModifiedAt = now,
            ), configNew
        )

        configService.putProjectConfig(
            "aRMT", "radar-test", ClientConfig(
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
                scope = "config.user.a",
                config = listOf(
                    SingleVariable("c", "b")
                ),
                defaults = listOf(
                    SingleVariable("d", "else", "config.project.radar-test")
                ),
                lastModifiedAt = now,
            ), configNull
        )
    }


    @Test
    fun projectConfig() {
        val now = Instant.now()
        val variableSet = VariableSet(
            id = null,
            scope = SimpleScope("config.project.radar-test"),
            variables = mapOf(
                QualifiedId("a.c") to "b".toVariable(),
                QualifiedId("a.d") to 5.toVariable(),
            ),
            lastModifiedAt = now,
        )

        resolver.update("aRMT", variableSet)
        println(resolver)
        assertEquals(
            ClientConfig(
                "aRMT",
                "config.project.radar-test",
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
            scope = SimpleScope("config.global"),
            variables = mapOf(
                QualifiedId("a.c") to "f".toVariable(),
                QualifiedId("a.e") to 5.toVariable(),
            ),
            lastModifiedAt = now,
        )
        resolver.update("aRMT", globalVariableSet)

        assertEquals(
            ClientConfig(
                "aRMT", "config.project.radar-test",
                listOf(
                    SingleVariable("a.c", "b"),
                    SingleVariable("a.d", "5")
                ),
                listOf(
                    SingleVariable("a.c", "f", "config.global"),
                    SingleVariable("a.e", "5", "config.global")
                ),
                lastModifiedAt = now,
            ),
            configService.projectConfig("aRMT", "radar-test")
        )

        assertEquals(
            ClientConfig(
                clientId = "aRMT",
                scope = "config.project.radar-demo",
                config = listOf(),
                defaults = listOf(
                    SingleVariable("a.c", "f", "config.global"),
                    SingleVariable("a.e", "5", "config.global"),
                ),
                lastModifiedAt = now,
            ),
            configService.projectConfig("aRMT", "radar-demo")
        )
    }

    @Test
    fun putProjectConfig() {
        val configEmpty = configService.projectConfig("aRMT", "radar-test")
        assertEquals(ClientConfig("aRMT", "config.project.radar-test", listOf()), configEmpty)
        configService.putProjectConfig(
            "aRMT", "radar-test", ClientConfig(
                null, null, listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5")
                )
            )
        )

        val config = configService.projectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                "aRMT", "config.project.radar-test", listOf(
                    SingleVariable("c", "b"),
                    SingleVariable("d", "5")
                )
            ), config
        )

        configService.putProjectConfig(
            "aRMT", "radar-test", ClientConfig(
                null, null, listOf(
                    SingleVariable("c", "b")
                )
            )
        )
        val configNew = configService.projectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                "aRMT", "config.project.radar-test", listOf(
                    SingleVariable("c", "b")
                )
            ), configNew
        )

        configService.putProjectConfig(
            "aRMT", "radar-test", ClientConfig(
                null, null, listOf(
                    SingleVariable("c", null)
                )
            )
        )
        val configNull = configService.projectConfig("aRMT", "radar-test")
        assertEquals(
            ClientConfig(
                "aRMT", "config.project.radar-test", listOf(
                    SingleVariable("c", null)
                )
            ), configNull
        )
    }
}
