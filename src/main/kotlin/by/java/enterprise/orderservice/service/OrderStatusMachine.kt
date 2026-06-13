package by.java.enterprise.orderservice.service

import by.java.enterprise.orderservice.entity.OrderStatus
import org.springframework.stereotype.Component

@Component
class OrderStatusMachine {

    private val allowed: Map<OrderStatus, Set<OrderStatus>> = mapOf(
        OrderStatus.CREATED to setOf(OrderStatus.PENDING_PAYMENT),
        OrderStatus.PENDING_PAYMENT to setOf(OrderStatus.PAID, OrderStatus.CANCELLED),
        OrderStatus.PAID to setOf(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
        OrderStatus.PROCESSING to setOf(OrderStatus.IN_DELIVERY, OrderStatus.CANCELLED),
        OrderStatus.IN_DELIVERY to setOf(OrderStatus.READY_FOR_PICKUP),
        OrderStatus.READY_FOR_PICKUP to setOf(OrderStatus.COMPLETED),
        OrderStatus.COMPLETED to emptySet(),
        OrderStatus.CANCELLED to emptySet()
    )

    fun canTransition(from: OrderStatus, to: OrderStatus): Boolean =
        allowed[from]?.contains(to) ?: false

    fun validateTransition(from: OrderStatus, to: OrderStatus) {
        if (!canTransition(from, to)) {
            throw InvalidOrderStatusTransitionException(from, to)
        }
    }
}
