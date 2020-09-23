package koans.collections

import koans.collections.models.Customer
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/Sum/Task.kt
 */
class Sum {

    // Return the sum of prices of all products that a customer has ordered.
    // Note: the customer may order the same product for several times.
    fun Customer.getTotalOrderPrice(): Double = orders.flatMap { it.products }.sumByDouble { product -> product.price }

    @Test
    fun getTotalOrderPrice_test() {
        TODO()
    }
}