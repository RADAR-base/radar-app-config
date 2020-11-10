package org.radarbase.appconfig.config

import com.hazelcast.config.NetworkConfig

data class HazelcastConfig(
    val configPath: String? = null,
    val instanceName: String = "appconfig",
    val clusterName: String = "appconfig",
    val network: NetworkConfig = NetworkConfig().apply {
        join.apply {
            multicastConfig.apply {
                isEnabled = true
                multicastPort = 53215
            }
        }
    },
)
