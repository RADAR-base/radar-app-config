package org.radarbase.appconfig.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.radarbase.appconfig.inject.ClientVariableRepository
import org.radarbase.appconfig.persistence.VariableRepository
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.SimpleScope

class NonResolvingConfigServiceImplTest {
    @Mock
    private lateinit var clientVariableRepository: ClientVariableRepository

    @Mock
    private lateinit var variableRepository: VariableRepository

    private lateinit var service: NonResolvingConfigServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        service = NonResolvingConfigServiceImpl(clientVariableRepository)
        clientVariableRepository.stub {
            on { get("test-client") } doReturn variableRepository
        }
    }

    @Test
    fun testGetConfig() = runBlocking {
        val scopes = listOf(SimpleScope("global"))
        val id = QualifiedId("test.id")
        val prefix = QualifiedId("test")
        val version = 1
        
        variableRepository.stub {
            onBlocking { query(scopes, id, prefix, version) } doReturn emptySequence()
        }

        val result = service.getConfig("test-client", scopes, id, prefix, version)
        assertEquals(0, result.count())
    }
}
