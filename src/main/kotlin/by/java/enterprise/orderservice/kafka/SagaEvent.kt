package by.java.enterprise.orderservice.kafka

import java.util.UUID

data class OrderReservedEvent(
    val eventId: String,
    val orderId: UUID,
    val status: String,
    val reason: String? = null
)

data class PaymentCreatedEvent(
    val eventId: String,
    val paymentId: String,
    val orderId: String,
    val userId: String,
    val status: String,
    val confirmationUrl: String? = null,
    val providerPaymentId: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val occurredAt: String? = null
)

data class RollbackSuccessfullyEvent(
    val eventId: String,
    val orderId: UUID
)

data class ReserveProductEvent(
    val eventId: String,
    val orderId: UUID,
    val items: List<ReserveProductItem>
)

data class ReserveProductItem(
    val productId: UUID,
    val quantity: Int
)

data class MakePaymentEvent(
    val eventId: String,
    val orderId: UUID,
    val totalAmount: Double,
    val userId: UUID
)

data class RollbackReservationEvent(
    val eventId: String,
    val orderId: UUID,
    val items: List<ReserveProductItem>
)
