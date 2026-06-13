package by.java.enterprise.orderservice.dto.request

import by.java.enterprise.orderservice.entity.OrderStatus
import jakarta.validation.constraints.NotNull

data class ChangeStatusRequest(
    @field:NotNull
    val status: OrderStatus
)
