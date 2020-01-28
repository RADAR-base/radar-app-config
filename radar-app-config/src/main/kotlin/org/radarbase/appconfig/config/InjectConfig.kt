package org.radarbase.appconfig.config

import org.radarbase.appconfig.inject.ManagementPortalEnhancerFactory
import org.radarbase.jersey.config.EnhancerFactory

data class InjectConfig(
        val enhancerFactory: Class<out EnhancerFactory> = ManagementPortalEnhancerFactory::class.java)
