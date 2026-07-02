/*
 *
 *  *  Copyright 2026 The Hyve
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package org.radarbase.appconfig

import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer
import org.glassfish.jersey.test.DeploymentContext
import org.glassfish.jersey.test.JerseyTest
import org.glassfish.jersey.test.ServletDeploymentContext
import org.glassfish.jersey.test.TestProperties
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory
import org.glassfish.jersey.test.spi.TestContainerFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.radarbase.appconfig.api.ClientConfig
import org.radarbase.appconfig.config.ApplicationConfig
import org.radarbase.appconfig.service.ClientService
import org.radarbase.auth.authorization.AuthorizationOracle
import org.radarbase.datadashboard.backend.util.ProjectServiceStub
import org.radarbase.jersey.auth.AuthValidator
import org.radarbase.jersey.auth.disabled.DisabledAuthValidator
import org.radarbase.jersey.auth.disabled.DisabledAuthorizationOracle
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.service.ProjectService
import org.radarbase.lang.expression.ResolvedVariable
import kotlin.jvm.java

// These tests are not yet working because mocking/stubbing token validation is not yet working.
class AppconfigIntegrationTest : JerseyTest() {

    init {
        set(TestProperties.CONTAINER_PORT, "0")
    }

    override fun configure(): ResourceConfig {
        val config: ApplicationConfig = ConfigLoader.loadConfig("src/test/resources/appconfig.yml", emptyArray())
        val resourceConfig = ConfigLoader.loadResources(config.inject.enhancerFactory, config)
        val disabledAuthorizationOracle = DisabledAuthorizationOracle()
        val disabledAuthValidator = DisabledAuthValidator(config.auth)
        val clientService = mock<ClientService>()
        resourceConfig.register(object : AbstractBinder() {
            override fun configure() {
                bind(disabledAuthorizationOracle).to(AuthorizationOracle::class.java).ranked(1)
                bind(disabledAuthValidator).to(AuthValidator::class.java).ranked(1)
                bind(ProjectServiceStub()).to(ProjectService::class.java).ranked(1)
                bind(clientService).to(ClientService::class.java).ranked(1)
            }
        })
        return resourceConfig
    }

    override fun getTestContainerFactory(): TestContainerFactory {
        return GrizzlyWebTestContainerFactory()
    }

    // See https://stackoverflow.com/questions/37902211/test-case-for-testing-a-jersey-web-resource-using-grizzly-is-giving-me-404
    override fun configureDeployment(): DeploymentContext {
        return ServletDeploymentContext.forServlet(ServletContainer(configure())).build()
    }

    @Test
    fun testGetHealth() {
        // Added by the health enhancer from radar-jersey.
        val response = target("health").request().get()
        assertEquals(200, response.status)
    }

    @Test
    fun testSearch() {
        target("service/config/radar_dashboard/search")
            .request()
            .get()
            .use { response ->
                assertEquals(200, response.status)

            }
    }

    @Test
    fun testSearchWithScopes() {
        target("service/config/radar_dashboard/search")
            .queryParam("scope", "global")
            .queryParam("scope", "project:test-project")
            .request()
            .get()
            .use { response ->
                assertEquals(200, response.status)
                val response = response.readEntity(ClientConfig::class.java)
                assert(response.config.isNotEmpty())
            }
    }
//
//    @ParameterizedTest
//    @CsvSource(
//        "max, 15.0",
//        "min, 5.0",
//        "avg, 10.0",
//    )
//    fun testCalculateValues(func: String, expected: Double) {
//        target("project/project-1/subject/sub-1/topic/questionnaire_answer/category/baseline_questions/variable/Perceived_Pain_Score/values/$func")
//            .request()
//            .get()
//            .use { response ->
//                assertEquals(200, response.status)
//                assertEquals(expected, response.readEntity(Double::class.java))
//            }
//    }
//
//    @Test
//    fun testGetCount() {
//        target("project/project-1/subject/sub-1/topic/questionnaire_answer/category/baseline_questions/variable/Perceived_Pain_Score/observations/count")
//            .request()
//            .get()
//            .use { response ->
//                assertEquals(200, response.status)
//                assertEquals(3, response.readEntity(Int::class.java))
//            }
//    }
}
