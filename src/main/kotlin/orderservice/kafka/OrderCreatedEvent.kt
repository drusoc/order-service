package by.java.enterprise.orderservice.kafka

data class OrderCreatedEvent(
    val id: java.util.UUID,
    val userId: java.util.UUID,
    val status: String,
    val totalPrice: Double,
    val totalWeight: Double,
    val deliveryAddressId: java.util.UUID,
    val items: List<Item>,
    val createdAt: String
) {
    data class Item(
        val id: java.util.UUID,
        val productId: java.util.UUID,
        val title: String,
        val priceAtPurchase: Double,
        val discountAtPurchase: Short,
        val finalPricePerItem: Double,
        val quantity: Int,
        val sellerName: String
    )
}
