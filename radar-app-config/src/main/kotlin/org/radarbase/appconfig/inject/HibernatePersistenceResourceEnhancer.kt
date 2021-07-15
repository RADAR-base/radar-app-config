package org.radarbase.appconfig.inject

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.appconfig.config.HazelcastConfig
import org.radarbase.jersey.config.JerseyResourceEnhancer
import jakarta.inject.Singleton
import org.radarbase.appconfig.persistence.ConfigRepository
import org.radarbase.appconfig.persistence.HibernateConfigRepository

class HibernatePersistenceResourceEnhancer(private val hazelcastConfig: HazelcastConfig) : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        System.setProperty("hazelcast.logging.type", "slf4j");
        val hzConfig = if (hazelcastConfig.configPath != null) {
            com.hazelcast.internal.config.ConfigLoader.load(hazelcastConfig.configPath)
        } else {
            Config().apply {
                networkConfig = hazelcastConfig.network
            }
        }.apply {
            clusterName = hazelcastConfig.clusterName
            instanceName = hazelcastConfig.instanceName
        }

        val hazelcastInstance = Hazelcast.newHazelcastInstance(hzConfig)

        bind(hazelcastInstance)
            .to(HazelcastInstance::class.java)
            .`in`(Singleton::class.java)

        bind(HibernateConfigRepository::class.java)
            .to(ConfigRepository::class.java)
            .`in`(Singleton::class.java)
    }
}
