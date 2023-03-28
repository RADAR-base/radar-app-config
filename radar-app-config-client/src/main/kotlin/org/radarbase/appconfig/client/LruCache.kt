package org.radarbase.appconfig.client

import java.time.Duration
import java.time.Instant
import java.util.LinkedHashMap

/**
 * Least recently used cache. It is synchronized.
 *
 * @param maxAge maximum age that a cache item is considered valid.
 * @param capacity number of items in cache. If it is not greater than 0, this cache will not cache anything.
 */
@Suppress("unused")
class LruCache<K, V>(
    private val maxAge: Duration,
    private val capacity: Int,
) : Iterable<Pair<K, V>> {
    private val map: MutableMap<K, Node> = LinkedHashMap<K, Node>(16, 0.75f, true)

    /**
     * Get item from cache.
     * @param key cache key.
     * @return cached value if available and valid, otherwise `null`.
     */
    @Synchronized
    operator fun get(key: K): V? {
        val value: Node = map[key] ?: return null
        if (value.isExpired) {
            map.remove(key)
            return null
        }
        return value.value
    }

    /**
     * Whether a cache item exists for a given key.
     * @param key cache key.
     * @return whether a cached value is available, regardless of whether it is still valid.
     */
    @Synchronized
    operator fun contains(key: K): Boolean = map.containsKey(key)

    /**
     * Iterates over all key-value pairs that are valid at the time that [Iterator.hasNext] is called.
     */
    override operator fun iterator(): Iterator<Pair<K, V>> = object : AbstractIterator<Pair<K, V>>() {
        val iterator = map.entries.iterator()
        override fun computeNext() {
            synchronized(this@LruCache) {
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (!next.value.isExpired) {
                        setNext(next.key to next.value.value)
                        return
                    }
                }
                done()
            }
        }
    }

    /**
     * Get a cache item if present and valid, otherwise compute a new value for in the cache.
     */
    fun computeIfAbsent(key: K, computation: () -> V): V = get(key)
        ?: computation().let {
            synchronized(this@LruCache) {
                get(key) ?: run {
                    set(key, it)
                    it
                }
            }
        }

    /** Remove a cache item from the cache. */
    @Synchronized
    fun remove(key: K) {
        map.remove(key)
    }

    /** Remove a cache item from the cache. */
    operator fun minusAssign(key: K) = remove(key)

    /** Set a new cache item. */
    @Synchronized
    operator fun set(key: K, value: V) {
        if (capacity <= 0) return
        val oldValue = map.put(key, Node(value))

        if (oldValue == null && map.size > capacity) {
            val it = map.keys.iterator()
            it.next()
            it.remove()
        }
    }

    /** Purge all expired items from the cache. */
    @Synchronized
    fun purge() {
        map.values.removeIf { it.isExpired }
    }

    private inner class Node(val value: V) {
        val validUntil: Instant = Instant.now().plus(maxAge)

        val isExpired: Boolean
            get() = Instant.now().isAfter(validUntil)
    }
}
