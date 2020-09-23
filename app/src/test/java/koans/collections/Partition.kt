package koans.collections

import koans.collections.models.Customer
import koans.collections.models.Shop
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/Partition/Task.kt
 */
class Partition {

    // Return customers who have more undelivered orders than delivered
    fun Shop.getCustomersWithMoreUndeliveredOrdersThanDelivered(): Set<Customer> = customers.filter {
        val (delivered, undelivered) = it.orders.partition { it.isDelivered }
        undelivered.size > delivered.size
    }.toSet()

    @Test
    fun getCustomersWithMoreUndeliveredOrdersThanDelivered_test() {
        TODO()
    }
}