package nl.thehyve.lang.expression

import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.NoSuchElementException

internal fun <A, B> Stream<A>.zipOrNull(other: Stream<out B>): Stream<Pair<A?, B?>> {
    val splitA = spliterator()
    val splitB = other.spliterator()

    // Zipping looses SORTED characteristic
    val characteristics = splitA.characteristics() and splitB.characteristics() and
            Spliterator.SORTED.inv()

    val zipSize = kotlin.math.min(splitA.exactSizeIfKnown, splitB.exactSizeIfKnown)

    val iterA = Spliterators.iterator(splitA)
    val iterB = Spliterators.iterator(splitB)
    val iterPair = object : Iterator<Pair<A?, B?>> {
        override fun hasNext() = iterA.hasNext() || iterB.hasNext()

        override fun next(): Pair<A?, B?> {
            val nextA = iterA.hasNext()
            val nextB = iterB.hasNext()
            return when {
                nextA && nextB -> Pair(iterA.next(), iterB.next())
                nextA -> Pair(iterA.next(), null)
                nextB -> Pair(null, iterB.next())
                else -> throw NoSuchElementException()
            }
        }
    }

    val split = Spliterators.spliterator(iterPair, zipSize, characteristics)
    return StreamSupport.stream(split, isParallel || other.isParallel)
}
