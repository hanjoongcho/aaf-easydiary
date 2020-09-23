package koans.collections

import koans.collections.data.reSharper
import koans.collections.data.shop
import koans.collections.models.Customer
import koans.collections.models.Product
import koans.collections.models.Shop
import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/Fold/Task.kt
 */
class Fold {

    // Return the set of products that were ordered by every customer
    fun Shop.getSetOfProductsOrderedByEveryCustomer(customers:List<Customer> = this.customers): Set<Product> {
        val allProducts = customers.flatMap { it.orders.flatMap { it.products }}.toSet()
        return customers.fold(allProducts, {
            orderedByAll, customer ->
            orderedByAll.intersect(customer.orders.flatMap { it.products }.toSet())
        })
    }

    @Test
    fun getSetOfProductsOrderedByEveryCustomer_test1() {
        Assert.assertEquals(0, shop.getSetOfProductsOrderedByEveryCustomer().size)
    }

    @Test
    fun getSetOfProductsOrderedByEveryCustomer_test2() {
        val customers = shop.customers.filter { customer ->
            customer.orders.flatMap { order -> order.products }.contains(reSharper)
        }
        Assert.assertArrayEquals(arrayOf(reSharper) , shop.getSetOfProductsOrderedByEveryCustomer(customers).toTypedArray())
    }
}