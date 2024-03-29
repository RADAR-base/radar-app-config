package org.radarbase.lang.expression

import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.math.min

internal fun <A, B> Stream<A>.zipOrNull(other: Stream<out B>): Stream<Pair<A?, B?>> {
    val splitA = spliterator()
    val splitB = other.spliterator()

    // Zipping looses SORTED characteristic
    val characteristics = (
        splitA.characteristics()
            and splitB.characteristics()
            and Spliterator.SORTED.inv()
        )

    val zipSize = min(splitA.exactSizeIfKnown, splitB.exactSizeIfKnown)

    val iterPair = object : AbstractIterator<Pair<A?, B?>>() {
        val iterA = Spliterators.iterator(splitA)
        val iterB = Spliterators.iterator(splitB)

        override fun computeNext() {
            val a = if (iterA.hasNext()) iterA.next() else null
            val b = if (iterB.hasNext()) iterB.next() else null
            if (a != null || b != null) {
                setNext(Pair(a, b))
            } else {
                done()
            }
        }
    }

    val split = Spliterators.spliterator(iterPair, zipSize, characteristics)
    return StreamSupport.stream(split, isParallel || other.isParallel)
}
