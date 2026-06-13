package by.java.enterprise.orderservice.dto.response

import by.java.enterprise.orderservice.entity.OrderStatus
import java.time.LocalDateTime
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val userId: UUID,
    val status: OrderStatus,
    val totalPrice: Double,
    val totalWeight: Double,
    val deliveryAddressId: UUID,
    val paymentTransactionId: UUID?,
    val deliveryId: UUID?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val items: List<OrderItemResponse>
)

data class OrderItemResponse(
    val id: UUID,
    val productId: UUID,
    val title: String,
    val priceAtPurchase: Double,
    val discountAtPurchase: Short,
    val finalPricePerItem: Double,
    val quantity: Int,
    val sellerName: String,
    val article: String,
    val barcode: String,
    val weight: Double,
    val width: Double,
    val height: Double,
    val depth: Double
)
