package koans.collections

import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/Get%20used%20to%20new%20style/Task.kt
 */
class GetUsedToNewStyle {

    fun doSomethingStrangeWithCollection(collection: Collection<String>): Collection<String>? {

        val groupsByLength = collection.groupBy { s -> s.length }

        val maximumSizeOfGroup = groupsByLength.values.map { group -> group.size }.max()

        return groupsByLength.values.firstOrNull { group -> group.size == maximumSizeOfGroup }
    }

    @Test
    fun doSomethingStrangeWithCollection_test() {
        TODO()
    }
}