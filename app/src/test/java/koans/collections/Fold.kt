package koans.collections

import koans.collections.models.Product
import koans.collections.models.Shop
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Collections/Fold/Task.kt
 */
class Fold {

    // Return the set of products that were ordered by every customer
    fun Shop.getSetOfProductsOrderedByEveryCustomer(): Set<Product> {
        val allProducts = customers.flatMap { it.orders.flatMap { it.products }}.toSet()
        return customers.fold(allProducts, {
            orderedByAll, customer ->
            orderedByAll.intersect(customer.orders.flatMap { it.products }.toSet())
        })
    }

    @Test
    fun getSetOfProductsOrderedByEveryCustomer_test() {
        TODO()
    }
}