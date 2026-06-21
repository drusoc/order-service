package by.java.enterprise.orderservice.service

import by.java.enterprise.orderservice.entity.OrderStatus

class InvalidOrderStatusTransitionException(
    from: OrderStatus,
    to: OrderStatus
) : RuntimeException("Cannot transition from $from to $to")
