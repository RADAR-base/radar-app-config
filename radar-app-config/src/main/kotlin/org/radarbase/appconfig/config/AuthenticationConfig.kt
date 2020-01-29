package org.radarbase.appconfig.config

import java.net.URL

data class AuthenticationConfig(
        val url: URL = URL("http://managementportal:8080/managementportal/"),
        val resourceName: String = "res_appconfig",
        val clientId: String = "appconfig",
        val clientSecret: String? = null)
