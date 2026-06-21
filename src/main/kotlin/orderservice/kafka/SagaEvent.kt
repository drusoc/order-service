package by.java.enterprise.orderservice.kafka

import java.time.Instant
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

data class DeliveryRequiredEvent(
    val eventId: String,
    val orderId: String,
    val userId: String,
    val tariffCode: String,
    val senderCityCode: String,
    val recipientCityCode: String,
    val pickupPointCode: String,
    val recipient: DeliveryRecipient,
    val address: String? = null,
    val packages: List<DeliveryPackage>,
    val comment: String? = null,
    val occurredAt: String = Instant.now().toString()
)

data class DeliveryRecipient(
    val name: String,
    val phone: String
)

data class DeliveryPackage(
    val number: String,
    val weightGram: Int,
    val lengthCm: Int,
    val widthCm: Int,
    val heightCm: Int
)

data class DeliveryStatusChangedEvent(
    val deliveryId: UUID,
    val orderId: String,
    val previousStatus: String,
    val status: String,
    val occurredAt: String? = null
)

data class DeliveryFailedEvent(
    val deliveryId: UUID,
    val orderId: String,
    val userId: String,
    val status: String,
    val reason: String,
    val occurredAt: String? = null
)

data class DeliveryCanceledEvent(
    val deliveryId: UUID,
    val orderId: String,
    val userId: String,
    val occurredAt: String? = null
)
