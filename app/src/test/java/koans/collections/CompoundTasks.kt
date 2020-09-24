package koans.collections

import koans.collections.models.Customer
import koans.collections.models.Product
import koans.collections.models.Shop

class CompoundTasks {
    // Return the most expensive product among all delivered products
    // (use the Order.isDelivered flag)
    fun Customer.getMostExpensiveDeliveredProduct(): Product? {
        return orders.filter { it.isDelivered }.flatMap { order -> order.products }.maxBy { product -> product.price }
    }

    // Return how many times the given product was ordered.
    // Note: a customer may order the same product for several times.
    fun Shop.getNumberOfTimesProductWasOrdered(product: Product): Int {
        return customers.flatMap { customer -> customer.orders }.flatMap { order -> order.products }.count { it == product }
    }
}