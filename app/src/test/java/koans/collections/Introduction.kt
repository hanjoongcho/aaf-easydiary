package koans.collections

import koans.collections.data.shop
import koans.collections.models.Customer
import koans.collections.models.Shop
import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/Introduction/Task.kt
 */
class Introduction {
    private fun Shop.getSetOfCustomers(): Set<Customer> = customers.toSet()

    @Test
    fun getSetOfCustomers_test() {
        Assert.assertEquals(6, shop.getSetOfCustomers().size)
    }
}

