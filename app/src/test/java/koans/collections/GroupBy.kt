package koans.collections

import koans.collections.models.City
import koans.collections.models.Customer
import koans.collections.models.Shop
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/GroupBy/Task.kt
 */
class GroupBy {
    // Return a map of the customers living in each city
    fun Shop.groupCustomersByCity(): Map<City, List<Customer>> = customers.groupBy { it.city }

    @Test
    fun groupCustomersByCity_test() {
        TODO()
    }
}