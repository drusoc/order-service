package by.java.enterprise.orderservice.entity

import com.fasterxml.jackson.annotation.JsonCreator

enum class OrderStatus {
    CREATED,
    PENDING_PAYMENT,
    PAID,
    PROCESSING,
    IN_DELIVERY,
    READY_FOR_PICKUP,
    COMPLETED,
    CANCELLED;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): OrderStatus =
            valueOf(value.uppercase())
    }
}
