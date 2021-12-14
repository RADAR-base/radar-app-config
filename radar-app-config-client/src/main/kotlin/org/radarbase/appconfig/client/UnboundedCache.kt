package org.radarbase.appconfig.client

import java.util.concurrent.ConcurrentHashMap

class UnboundedCache<K, V> : Cache<K, V> {
    private val cache = ConcurrentHashMap<K, V>()

    override suspend fun get(key: K): V? = cache[key]

    override suspend fun put(key: K, value: V) {
        cache[key] = value
    }

    override suspend fun putIfAbsent(key: K, value: V) {
        cache.putIfAbsent(key, value)
    }

    override suspend fun remove(key: K, value: V) {
        cache.remove(key, value)
    }
}
