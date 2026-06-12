package org.radarbase.appconfig.config

import org.radarbase.appconfig.inject.ApplicationEnhancerFactory
import org.radarbase.jersey.enhancer.EnhancerFactory

data class InjectConfig(
    val enhancerFactory: Class<out EnhancerFactory> = ApplicationEnhancerFactory::class.java,
)
