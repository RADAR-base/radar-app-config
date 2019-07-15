package org.radarbase.appconfig.util

import java.time.Duration
import java.time.Instant

class CachedSet<T>(
        private val refreshDuration: Duration,
        private val retryDuration: Duration,
        private val supplier: () -> Iterable<T>) {
    @set:Synchronized
    private var cached: Set<T> = emptySet()
        set(value) {
            val now = Instant.now()
            field = value
            nextRefresh = now.plus(refreshDuration)
            nextRetry = now.plus(retryDuration)
        }

    private var nextRefresh: Instant = Instant.MIN
    private var nextRetry: Instant = Instant.MIN

    @get:Synchronized
    private val state: State
        get () {
            val now = Instant.now()
            return State(cached,
                    now.isAfter(nextRefresh),
                    now.isAfter(nextRetry))
        }

    private fun refresh() = supplier.invoke().toSet()
            .also { cached = it }

    fun contains(value: T) = state.query({ it.contains(value) }, { it })
    fun find(predicate: (T) -> Boolean): T? = state.query({ it.find(predicate) }, { it != null })
    fun get(): Set<T> = state.query({ it }, { it.isNotEmpty() })

    private inner class State(val cache: Set<T>, val mustRefresh: Boolean, val mayRetry: Boolean) {
        fun <S> query(method: (Set<T>) -> S, validityPredicate: (S) -> Boolean): S {
            var result: S
            if (mustRefresh) {
                result = method(refresh())
            } else {
                result = method(cache)
                if (!validityPredicate(result) && mayRetry) {
                    result = method(refresh())
                }
            }
            return result
        }
    }
}
