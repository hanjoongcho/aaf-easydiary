package koans.collections

import koans.collections.data.shop
import koans.collections.models.City
import koans.collections.models.Customer
import koans.collections.models.Shop
import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/All%20Any%20and%20other%20predicates/Task.kt
 */
class AllAnyAndOtherPredicates {

    // Return true if all customers are from the given city
    private fun Shop.checkAllCustomersAreFrom(city: City): Boolean = customers.all { customer -> customer.city == city }

    // Return true if there is at least one customer from the given city
    private fun Shop.hasCustomerFrom(city: City): Boolean = customers.any { customer -> customer.city == city }

    // Return the number of customers from the given city
    private fun Shop.countCustomersFrom(city: City): Int = customers.count { customer -> customer.city == city }

    // Return a customer who lives in the given city, or null if there is none
    private fun Shop.findAnyCustomerFrom(city: City): Customer? = customers.find { customer -> customer.city == city }

    @Test
    fun checkAllCustomersAreFrom_test() {
        Assert.assertFalse(shop.checkAllCustomersAreFrom(City("Canberra")))
    }

    @Test
    fun hasCustomerFrom_test() {
        Assert.assertTrue(shop.hasCustomerFrom(City("Canberra")))
    }

    @Test
    fun countCustomersFrom_test() {
        Assert.assertEquals(2, shop.countCustomersFrom(City("Canberra")))
    }
}