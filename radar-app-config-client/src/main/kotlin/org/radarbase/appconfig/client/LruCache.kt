package org.radarbase.appconfig.client

import java.time.Duration
import java.time.Instant
import java.util.LinkedHashMap

@Suppress("unused")
class LruCache<K, V>(private val maxAge: Duration, private val capacity: Int): Iterable<Pair<K, V>> {
    private val map: MutableMap<K, Node> = LinkedHashMap<K, Node>(16, 0.75f, true)

    @Synchronized
    operator fun get(key: K): V? {
        val value: Node = map[key] ?: return null
        if (value.isExpired) {
            map.remove(key)
            return null
        }
        return value.value
    }

    @Synchronized
    operator fun contains(key: K): Boolean = map.containsKey(key)

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

    fun computeIfAbsent(key: K, computation: () -> V): V = get(key)
        ?: computation().also { set(key, it) }

    @Synchronized
    fun remove(key: K) {
        map.remove(key)
    }

    operator fun minusAssign(key: K) = remove(key)

    @Synchronized
    operator fun set(key: K, value: V) {
        val oldValue = map.put(key, Node(value))

        if (oldValue == null && map.size > capacity) {
            val it = map.keys.iterator()
            it.next()
            it.remove()
        }
    }

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
