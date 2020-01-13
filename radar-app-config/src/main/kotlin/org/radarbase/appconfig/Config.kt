package org.radarbase.appconfig

import org.radarbase.appconfig.inject.ManagementPortalEnhancerFactory
import org.radarbase.jersey.config.EnhancerFactory
import java.net.URI
import java.net.URL

class Config {
    var clientId: String = "appconfig"
    var clientSecret: String? = null
    var baseUri: URI = URI.create("http://0.0.0.0:8090/appconfig/")
    var managementPortalUrl: URL = URL("http://managementportal-app:8080/managementportal/")
    var resourceConfig: Class<out EnhancerFactory> = ManagementPortalEnhancerFactory::class.java
    var jwtResourceName: String = "res_appconfig"
    var isJmxEnabled: Boolean = true
}
