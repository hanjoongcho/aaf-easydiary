package koans.collections

import koans.collections.models.Customer
import koans.collections.models.Shop
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/Sort/Task.kt
 */
class Sort {
    fun Shop.getCustomersSortedByNumberOfOrders(): List<Customer> = customers.sortedBy { it.orders.size }

    @Test
    fun getCustomersSortedByNumberOfOrders_test() {
        TODO()
    }
}