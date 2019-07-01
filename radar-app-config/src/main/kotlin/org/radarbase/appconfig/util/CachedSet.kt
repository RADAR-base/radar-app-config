package org.radarbase.appconfig.util

import java.time.Duration
import java.time.Instant

class CachedSet<in T>(
        private val refreshDuration: Duration,
        private val retryDuration: Duration,
        private val supplier: () -> Iterable<T>) {

    private var cached: Set<T> = emptySet()
    private var nextRefresh: Instant = Instant.MIN
    private var nextRetry: Instant = Instant.MIN

    fun contains(value: T): Boolean {
        val now = Instant.now()
        val (localSet, mustRefresh, mayRetry) = synchronized(this) {
            Triple(cached,
                    now.isAfter(nextRefresh),
                    now.isAfter(nextRetry))
        }

        val containsValue = localSet.contains(value)

        return if (mustRefresh || (!containsValue && mayRetry)) {
            val updatedSet = supplier.invoke().toSet()
            synchronized(this) {
                cached = updatedSet
                nextRefresh = now.plus(refreshDuration)
                nextRetry = now.plus(retryDuration)
            }
            updatedSet.contains(value)
        } else {
            containsValue
        }
    }
}
