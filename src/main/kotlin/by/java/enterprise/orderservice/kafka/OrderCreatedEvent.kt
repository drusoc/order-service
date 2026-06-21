package by.java.enterprise.orderservice.kafka

import java.util.*

data class OrderCreatedEvent(
    val id: UUID,
    val userId: UUID,
    val status: String,
    val totalPrice: Double,
    val totalWeight: Double,
    val deliveryAddressId: UUID,
    val items: List<Item>,
    val createdAt: String
) {
    data class Item(
        val id: UUID,
        val productId: UUID,
        val title: String,
        val priceAtPurchase: Double,
        val discountAtPurchase: Short,
        val finalPricePerItem: Double,
        val quantity: Int,
        val sellerName: String
    )
}
