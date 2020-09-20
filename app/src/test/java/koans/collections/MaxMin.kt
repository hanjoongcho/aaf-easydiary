package koans.collections

import koans.collections.models.Customer
import koans.collections.models.Product
import koans.collections.models.Shop

class MaxMin {
    // Return a customer whose order count is the highest among all customers
    fun Shop.getCustomerWithMaximumNumberOfOrders(): Customer? = customers.maxBy { it.orders.size  }

    // Return the most expensive product which has been ordered
    fun Customer.getMostExpensiveOrderedProduct(): Product? = orders.flatMap { it.products }.maxBy { it.price }
}