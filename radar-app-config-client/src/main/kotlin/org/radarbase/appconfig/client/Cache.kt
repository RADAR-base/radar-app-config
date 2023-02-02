package org.radarbase.appconfig.client

interface Cache<K, V> {
    suspend fun get(key: K): V?
    suspend fun put(key: K, value: V)
    suspend fun putIfAbsent(key: K, value: V)
    suspend fun remove(key: K)
}
