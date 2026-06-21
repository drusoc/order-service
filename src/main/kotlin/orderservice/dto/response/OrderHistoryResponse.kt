package by.java.enterprise.orderservice.dto.response

import by.java.enterprise.orderservice.entity.OrderStatus
import java.time.LocalDateTime
import java.util.UUID

data class OrderHistoryResponse(
    val orders: List<OrderSummaryResponse>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)

data class OrderSummaryResponse(
    val id: UUID,
    val userId: UUID,
    val status: OrderStatus,
    val totalPrice: Double,
    val totalWeight: Double,
    val deliveryAddressId: UUID,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
