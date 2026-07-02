package org.radarbase.appconfig.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.core.Application
import jakarta.ws.rs.core.GenericType
import jakarta.ws.rs.core.MediaType
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.test.JerseyTest
import org.glassfish.jersey.test.TestProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking
import org.radarbase.appconfig.persistence.MockAsyncCoroutineService
import org.radarbase.appconfig.resource.paramconverter.ScopeParamConverterProvider
import org.radarbase.appconfig.service.ClientService
import org.radarbase.appconfig.service.NonResolvingConfigService
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.lang.expression.QualifiedId

class ServiceResourceTest : JerseyTest() {
    init {
        set(TestProperties.CONTAINER_PORT, "0")
    }

    @Mock
    lateinit var configService: NonResolvingConfigService

    @Mock
    lateinit var clientService: ClientService

    class TestResourceEnhancer : JerseyResourceEnhancer {
        override val classes: Array<Class<*>> = arrayOf(
            ScopeParamConverterProvider::class.java,
        )

        override val packages: Array<String> = arrayOf(
            "org.radarbase.appconfig.resource",
        )

        override fun org.glassfish.jersey.internal.inject.AbstractBinder.enhance() {
            bind(MockAsyncCoroutineService())
                .to(AsyncCoroutineService::class.java)
                .`in`(Singleton::class.java)
        }
    }

    class TestEnhancerFactory : EnhancerFactory {
        override fun createEnhancers(): List<JerseyResourceEnhancer> = listOf(
            TestResourceEnhancer(),
            Enhancers.mapper,
            Enhancers.exception,
        )
    }

    override fun configure(): Application {
        MockitoAnnotations.openMocks(this)
        val resourceConfig = ConfigLoader.loadResources(TestEnhancerFactory::class.java)
        resourceConfig.register(object : AbstractBinder() {
            override fun configure() {
                bind(configService).to(NonResolvingConfigService::class.java)
                bind(clientService).to(ClientService::class.java)
            }
        })
        return resourceConfig
    }

    @Test
    fun testGetGlobalConfig() {
        val clientId = "test-client"
        val name = "app_name"
        val project = "test-project"
        val version = 3

        configService.stub {
            onBlocking {
                getConfig(
                    eq(clientId),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull()
                )
            } doReturn emptySequence()
        }

        val response = target("service/config/$clientId/search")
            .queryParam("scope", "global")
            .queryParam("scope", "project:$project")
            .queryParam("name", name)
            .queryParam("version", version)
            .request(MediaType.APPLICATION_JSON)
            .get()

        assertEquals(200, response.status)
        val entity = response.readEntity(object : GenericType<List<Any>>() {})
        assertEquals(0, entity.size)

        verifyBlocking(clientService) {
            ensureClient(clientId)
        }
        verifyBlocking(configService) {
            getConfig(
                clientId = eq(clientId),
                scopes = argThat {
                    size == 2 && first().asString() == "global"
                        && last().asString() == "project:$project"
                },
                id = eq(QualifiedId(name)),
                prefix = anyOrNull(),
                version = eq(version),
            )
        }
    }

}
