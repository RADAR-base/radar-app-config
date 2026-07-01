package org.radarbase.appconfig.persistence

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.hibernate.cfg.Configuration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.radarbase.appconfig.persistence.entity.ConfigEntity
import java.sql.DriverManager
import java.util.Properties

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class RepositoryTest {
    protected lateinit var emf: EntityManagerFactory
    protected lateinit var em: EntityManager

    @BeforeAll
    fun setUp() {
        val url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DEFAULT_NULL_ORDERING=HIGH;NON_KEYWORDS=VALUE;INIT=create schema if not exists $SCHEMA_NAME"
        val user = "sa"
        val password = ""

        DriverManager.getConnection(url, user, password).use {
            val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(it))
            database.liquibaseSchemaName = null
            database.defaultSchemaName = SCHEMA_NAME
            val liquibase = Liquibase(
                "db/changelog/changes/db.changelog-master.xml",
                ClassLoaderResourceAccessor(),
                database,
            )
            liquibase.update(LIQUIBASE_CONTEXT)
        }

        val props = Properties()
        props["jakarta.persistence.jdbc.url"] = url
        props["jakarta.persistence.jdbc.user"] = user
        props["jakarta.persistence.jdbc.password"] = password
        props["jakarta.persistence.jdbc.driver"] = "org.h2.Driver"
        props["hibernate.dialect"] = "org.hibernate.dialect.H2Dialect"
        props["hibernate.show_sql"] = "true"
        props["hibernate.format_sql"] = "true"
        props["jakarta.persistence.schema-generation.database.action"] = "none"
        props["hibernate.default_schema"] = SCHEMA_NAME

        val configuration = Configuration()
        configuration.addAnnotatedClass(ConfigEntity::class.java)
        configuration.addProperties(props)

        emf = configuration.buildSessionFactory()
        em = emf.createEntityManager()
    }

    @AfterAll
    fun tearDown() {
        if (this::em.isInitialized) em.close()
        if (this::emf.isInitialized) emf.close()
    }

    companion object {
        const val SCHEMA_NAME = "CONFIG"
        const val LIQUIBASE_CONTEXT = "dev"
    }
}
