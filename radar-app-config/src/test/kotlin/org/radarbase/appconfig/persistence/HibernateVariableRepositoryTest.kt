package org.radarbase.appconfig.persistence

import jakarta.inject.Provider
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.lang.expression.QualifiedId
import org.radarbase.lang.expression.SimpleScope
import kotlin.streams.toList

class HibernateVariableRepositoryTest : RepositoryTest() {
    private lateinit var repository: HibernateVariableRepository

    @BeforeEach
    fun setUpRepository() {
        val emProvider = Provider { em }
        val asyncService = MockAsyncCoroutineService()
        repository = HibernateVariableRepository(emProvider, "radar_dashboard", asyncService)
    }

    @Test
    fun testQueryAll() = runBlocking {
        val variables = repository.query().toList()
        assertEquals(3, variables.size)
    }

    @Test
    fun testQueryByScope() = runBlocking {
        val variables = repository.query(scopes = listOf(SimpleScope("global"))).toList()
        assertEquals(1, variables.size)
        assertEquals("app_name", variables[0].name)
        assertEquals("Radar Dashboard", variables[0].value)
    }

    @Test
    fun testQueryByIdReturnsLatestVersion() = runBlocking {
        val variables = repository.query(id = QualifiedId("theme")).toList()
        assertEquals(1, variables.size)
        assertEquals("project:test-project", variables[0].scope)
        assertEquals("light", variables[0].value)
    }

    @Test
    fun testQueryByIdAndVersion() = runBlocking {
        val variables = repository.query(id = QualifiedId("theme"), version = 1).toList()
        assertEquals(1, variables.size)
        assertEquals("project:test-project", variables[0].scope)
        assertEquals("dark", variables[0].value)
    }

    @Test
    fun testQueryByPrefix() = runBlocking {
        val variables = repository.query(prefix = QualifiedId("app")).toList()
        assertEquals(1, variables.size)
        assertEquals("app_name", variables[0].name)
    }
}
