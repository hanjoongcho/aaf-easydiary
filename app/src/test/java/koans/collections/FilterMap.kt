package koans.collections

import koans.collections.data.shop
import koans.collections.models.City
import koans.collections.models.Customer
import koans.collections.models.Shop
import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/Filter%20map/Task.kt
 */
class FilterMap {
    // Return the set of cities the customers are from
    fun Shop.getCitiesCustomersAreFrom(): Set<City> = customers.map { customer -> customer.city }.toSet()

    // Return a list of the customers who live in the given city
    fun Shop.getCustomersFrom(city: City): List<Customer> = customers.filter { customer -> customer.city == city }

    @Test
    fun getCitiesCustomersAreFrom_test1() {
        Assert.assertEquals("Canberra, Vancouver, Budapest, Ankara, Tokyo",  shop.getCitiesCustomersAreFrom().joinToString())
    }

    @Test
    fun getCustomersFrom_test1() {
        Assert.assertEquals(1, shop.getCustomersFrom(City("Tokyo")).size)
    }
}