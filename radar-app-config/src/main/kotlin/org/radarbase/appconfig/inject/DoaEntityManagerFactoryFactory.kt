/*
 *
 *  * Copyright 2019 The Hyve
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package org.radarbase.appconfig.inject

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.LiquibaseException
import liquibase.resource.ClassLoaderResourceAccessor
import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.hibernate.internal.SessionImpl
import org.radarbase.appconfig.config.ApplicationConfig
import org.slf4j.LoggerFactory
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import javax.ws.rs.core.Context

class DoaEntityManagerFactoryFactory(
        @Context config: ApplicationConfig
) : DisposableSupplier<EntityManagerFactory> {
    @Suppress("UNCHECKED_CAST")
    private val configMap = (
            mapOf(
                    "javax.persistence.jdbc.driver" to config.jdbc?.driver,
                    "javax.persistence.jdbc.url" to config.jdbc?.url,
                    "javax.persistence.jdbc.user" to config.jdbc?.user,
                    "javax.persistence.jdbc.password" to config.jdbc?.password)
                    + (config.jdbc?.properties ?: emptyMap()))
            .filterValues { it != null } as Map<String, String>

    override fun get(): EntityManagerFactory {
        logger.info("Initializing EntityManagerFactory with config: $configMap")
        return Persistence.createEntityManagerFactory("org.radarbase.appconfig.domain", configMap)
                .also { initializeDatabase(it) }
    }

    private fun initializeDatabase(emf: EntityManagerFactory) {
        logger.info("Initializing Liquibase")
        val connection = emf.createEntityManager().unwrap(SessionImpl::class.java).connection()
        try {
            val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
            val liquibase = Liquibase("dbChangelog.xml", ClassLoaderResourceAccessor(), database)
            liquibase.update("test")
        } catch (e: LiquibaseException) {
            logger.error("Failed to initialize database", e)
        }
    }

    override fun dispose(instance: EntityManagerFactory?) {
        logger.info("Disposing EntityManagerFactory")
        instance?.close()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DoaEntityManagerFactoryFactory::class.java)
    }
}
