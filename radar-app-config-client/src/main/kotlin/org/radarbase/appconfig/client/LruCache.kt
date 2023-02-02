package org.radarbase.appconfig.client

import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Least recently used cache. It is synchronized.
 *
 * @param maxAge maximum age that a cache item is considered valid.
 * @param capacity number of items in cache. If it is not greater than 0, this cache will not cache anything.
 */
@Suppress("unused")
class LruCache<K, V>(
    private val maxAge: Duration,
    private val capacity: Int
): Cache<K, V> {
    private val map: MutableMap<K, Node> = LinkedHashMap(16, 0.75f, true)
    private val mutex = Mutex()

    /**
     * Get item from cache.
     * @param key cache key.
     * @return cached value if available and valid, otherwise `null`.
     */
    override suspend fun get(key: K): V? = mutex.withLock {
        val value: Node = map[key] ?: return null
        if (value.isExpired) {
            map.remove(key)
            return null
        }
        return value.value
    }

    /** Remove a cache item from the cache. */
    override suspend fun remove(key: K): Unit = mutex.withLock {
        map.remove(key)
    }

    /** Set a new cache item. */
    override suspend fun put(key: K, value: V) = mutex.withLock {
        if (capacity <= 0) return
        val oldValue = map.put(key, Node(value))

        if (oldValue == null) {
            removeOldestIfNeeded()
        }
    }

    private fun removeOldestIfNeeded() {
        val it = map.keys.iterator()
        while (map.size > capacity) {
            it.next()
            it.remove()
        }
    }

    /** Set a new cache item. */
    override suspend fun putIfAbsent(key: K, value: V) = mutex.withLock {
        if (capacity <= 0) return@withLock

        val oldValue = map[key]
        if (oldValue == null || oldValue.isExpired) {
            map[key] = Node(value)
            removeOldestIfNeeded()
        }
    }

    private inner class Node(val value: V) {
        val validUntil: Instant = Instant.now().plus(maxAge)

        val isExpired: Boolean
            get() = Instant.now().isAfter(validUntil)
    }
}
