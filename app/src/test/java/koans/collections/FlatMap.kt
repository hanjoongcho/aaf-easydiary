package koans.collections

import koans.collections.models.Customer
import koans.collections.models.Product
import koans.collections.models.Shop
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/FlatMap/Task.kt
 */
class FlatMap {

    // Return all products this customer has ordered
    private val Customer.orderedProducts: Set<Product> get() {
        return orders.flatMap { it.products }.toSet()
    }

    // Return all products that were ordered by at least one customer
    private val Shop.allOrderedProducts: Set<Product> get() {
        return customers.flatMap { it.orderedProducts }.toSet()
    }

    @Test
    fun orderedProducts_test() {
        TODO()
    }

    @Test
    fun allOrderedProducts_test() {
        TODO()
    }
}