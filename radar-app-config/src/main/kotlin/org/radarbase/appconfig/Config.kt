package org.radarbase.appconfig

import org.radarbase.appconfig.inject.ManagementPortalAppConfigResources
import java.net.URI

class Config {
    var baseUri: URI = URI.create("http://0.0.0.0:8090/appconfig/")
    var managementPortalUrl: String = "http://managementportal-app:8080/managementportal/"
    var resourceConfig: Class<*> = ManagementPortalAppConfigResources::class.java
    var jwtKeystorePath: String? = null
    var jwtKeystoreAlias: String? = null
    var jwtKeystorePassword: String? = null
    var jwtECPublicKeys: List<String>? = null
    var jwtRSAPublicKeys: List<String>? = null
    var jwtIssuer: String? = null
    var jwtResourceName: String = "res_gateway"
    var isJmxEnabled: Boolean = true
}
