package koans.generics

import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Generics/Generic%20functions/Task.kt
 */
class GenericFunctions {

    private fun <T, C: MutableCollection<T>> Collection<T>.partitionTo(first: C, second: C, predicate: (T) -> Boolean): Pair<C, C> {
        for (element in this) {
            if (predicate(element)) {
                first.add(element)
            } else {
                second.add(element)
            }
        }
        return Pair(first, second)
    }

    @Test
    fun partitionWordsAndLines_test() {
        val (words, lines) = listOf("a", "a b", "c", "d e").
                partitionTo(ArrayList<String>(), ArrayList()) { s -> !s.contains(" ") }
        Assert.assertArrayEquals(arrayOf("a", "c"), words.toArray())
        Assert.assertArrayEquals(arrayOf("a b", "d e"), lines.toArray())
    }

    @Test
    fun partitionLettersAndOtherSymbols_test() {
        val (letters, other) = setOf('a', '%', 'r', '}').
        partitionTo(HashSet<Char>(), HashSet()) { c -> c in 'a'..'z' || c in 'A'..'Z'}
        Assert.assertArrayEquals(arrayOf('a', 'r'), letters.toArray())
        Assert.assertArrayEquals(arrayOf('%', '}'), other.toArray())
    }
}