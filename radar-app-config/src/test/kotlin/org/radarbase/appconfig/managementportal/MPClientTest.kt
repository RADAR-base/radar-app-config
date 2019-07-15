package org.radarbase.appconfig.managementportal

import org.junit.jupiter.api.Test
import org.radarbase.appconfig.Config
import java.net.URL

internal class MPClientTest {
    @Test
    fun testProjects() {
        val config = Config().apply {
            managementPortalUrl = URL("https://radar-test.thehyve.net/managementportal/")
            clientSecret = "appconfig_test"
        }

        val client = MPClient(config, MockAuth())
        println(client.readProjects())
    }
}