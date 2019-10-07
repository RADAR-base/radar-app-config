package org.radarbase.appconfig.managementportal

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.radarbase.appconfig.Config
import org.radarbase.jersey.auth.Auth
import org.radarcns.auth.authorization.Permission
import org.radarcns.auth.token.RadarToken
import java.net.URL

internal class MPClientTest {
    @Test
    fun testProjects() {
        val config = Config().apply {
            managementPortalUrl = URL("https://radar-test.thehyve.net/managementportal/")
            clientSecret = "appconfig_test"
        }

        val mockToken = mock<RadarToken> {
            on { hasPermissionOnProject(eq(Permission.PROJECT_READ), anyString()) } doReturn true
        }
        val auth = mock<Auth> {
            on { token } doReturn mockToken
        }
        val client = MPClient(config, auth)
        println(client.readProjects())
    }
}