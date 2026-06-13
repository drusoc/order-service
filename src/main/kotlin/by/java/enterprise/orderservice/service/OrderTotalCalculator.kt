package by.java.enterprise.orderservice.service

import by.java.enterprise.orderservice.dto.request.CreateOrderItemRequest
import org.springframework.stereotype.Component

@Component
class OrderTotalCalculator {

    fun calculateTotalPrice(items: List<CreateOrderItemRequest>): Double =
        items.sumOf { it.finalPricePerItem * it.quantity }

    fun calculateTotalWeight(items: List<CreateOrderItemRequest>): Double =
        items.sumOf { it.weight * it.quantity }
}
