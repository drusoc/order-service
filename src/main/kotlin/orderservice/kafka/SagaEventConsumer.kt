package by.java.enterprise.orderservice.kafka

import by.java.enterprise.orderservice.entity.OrderStatus
import by.java.enterprise.orderservice.exception.OrderNotFoundException
import by.java.enterprise.orderservice.outbox.OutboxEntity
import by.java.enterprise.orderservice.outbox.OutboxRepository
import by.java.enterprise.orderservice.outbox.ProcessedEventEntity
import by.java.enterprise.orderservice.outbox.ProcessedEventRepository
import by.java.enterprise.orderservice.repository.OrderRepository
import by.java.enterprise.orderservice.service.OrderStatusMachine
import tools.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class SagaEventConsumer(
    private val objectMapper: ObjectMapper,
    private val orderRepository: OrderRepository,
    private val statusMachine: OrderStatusMachine,
    private val outboxRepository: OutboxRepository,
    private val processedEventRepository: ProcessedEventRepository
) {
    @KafkaListener(topics = ["\${app.kafka.topic.order-reserved}"])
    @Transactional
    fun onOrderReservedEvent(raw: String) {
        val event = objectMapper.readValue(raw, OrderReservedEvent::class.java)
        if (processedEventRepository.existsByEventId(event.eventId)) return

        val order = orderRepository.findById(event.orderId)
            .orElseThrow { OrderNotFoundException(event.orderId) }
        require(order.status == OrderStatus.CREATED || order.status == OrderStatus.PENDING_PAYMENT) {
            "Expected CREATED or PENDING_PAYMENT but got ${order.status} for order ${order.id}"
        }

        if (order.status == OrderStatus.CREATED) {
            order.status = OrderStatus.PENDING_PAYMENT
        }

        if (event.status == "SUCCESS") {
            val payload = MakePaymentEvent(
                eventId = UUID.randomUUID().toString(),
                orderId = order.id,
                totalAmount = order.totalPrice,
                userId = order.userId
            )
            saveOutbox("make-payment-event", order.id.toString(), payload)
        } else {
            statusMachine.validateTransition(order.status, OrderStatus.CANCELLED)
            order.status = OrderStatus.CANCELLED
            orderRepository.save(order)
            val payload = CancelledEvent(
                orderId = order.id,
                reason = event.reason ?: "reservation_failed"
            )
            saveOutbox("order-cancelled-event", order.id.toString(), payload)
        }
        processedEventRepository.save(ProcessedEventEntity(event.eventId, event.orderId, "order-reserved-event"))
    }

    @KafkaListener(topics = ["\${app.kafka.topic.payment-created}"])
    @Transactional
    fun onPaymentCreatedEvent(raw: String) {
        val event = objectMapper.readValue(raw, PaymentCreatedEvent::class.java)
        if (processedEventRepository.existsByEventId(event.eventId)) return

        val orderId = UUID.fromString(event.orderId)
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }
        require(order.status == OrderStatus.PENDING_PAYMENT) {
            "Expected PENDING_PAYMENT but got ${order.status} for order ${order.id}"
        }

        if (event.status == "SUCCESS") {
            statusMachine.validateTransition(order.status, OrderStatus.PAID)
            order.status = OrderStatus.PAID
            orderRepository.save(order)

            val deliveryEvent = DeliveryRequiredEvent(
                eventId = UUID.randomUUID().toString(),
                orderId = order.id.toString(),
                userId = order.userId.toString(),
                tariffCode = "136",
                senderCityCode = "44",
                recipientCityCode = "44",
                pickupPointCode = order.pickupPointCode ?: "EV1",
                recipient = DeliveryRecipient(
                    name = order.recipientName ?: "User",
                    phone = order.recipientPhone ?: "0000000000"
                ),
                packages = order.items.mapIndexed { i, item ->
                    DeliveryPackage(
                        number = (i + 1).toString(),
                        weightGram = (item.weight * 1000).toInt().coerceAtLeast(1),
                        lengthCm = item.width.toInt().coerceAtLeast(1),
                        widthCm = item.depth.toInt().coerceAtLeast(1),
                        heightCm = item.height.toInt().coerceAtLeast(1)
                    )
                }
            )
            saveOutbox("orders.delivery.required", orderId.toString(), deliveryEvent)
        } else {
            val payload = RollbackReservationEvent(
                eventId = UUID.randomUUID().toString(),
                orderId = order.id,
                items = order.items.map {
                    ReserveProductItem(productId = it.productId, quantity = it.quantity)
                }
            )
            saveOutbox("rollback-reservation-event", orderId.toString(), payload)
        }
        processedEventRepository.save(ProcessedEventEntity(event.eventId, orderId, "payment-created-event"))
    }

    @KafkaListener(topics = ["\${app.kafka.topic.delivery-status-changed}"])
    @Transactional
    fun onDeliveryStatusChanged(raw: String) {
        val event = objectMapper.readValue(raw, DeliveryStatusChangedEvent::class.java)
        val orderId = UUID.fromString(event.orderId)
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        when (event.status) {
            "WAITING_FOR_PROVIDER" -> {
                statusMachine.validateTransition(order.status, OrderStatus.PROCESSING)
                order.status = OrderStatus.PROCESSING
                order.deliveryId = event.deliveryId
            }
            "ACCEPTED" -> {
                statusMachine.validateTransition(order.status, OrderStatus.IN_DELIVERY)
                order.status = OrderStatus.IN_DELIVERY
                order.deliveryId = event.deliveryId
            }
            "READY_FOR_PICKUP" -> {
                statusMachine.validateTransition(order.status, OrderStatus.READY_FOR_PICKUP)
                order.status = OrderStatus.READY_FOR_PICKUP
            }
            "DELIVERED" -> {
                statusMachine.validateTransition(order.status, OrderStatus.COMPLETED)
                order.status = OrderStatus.COMPLETED
            }
        }
        orderRepository.save(order)
    }

    @KafkaListener(topics = ["\${app.kafka.topic.delivery-failed}"])
    @Transactional
    fun onDeliveryFailed(raw: String) {
        val event = objectMapper.readValue(raw, DeliveryFailedEvent::class.java)
        val orderId = UUID.fromString(event.orderId)
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        statusMachine.validateTransition(order.status, OrderStatus.CANCELLED)
        order.status = OrderStatus.CANCELLED
        orderRepository.save(order)
    }

    @KafkaListener(topics = ["\${app.kafka.topic.delivery-canceled}"])
    @Transactional
    fun onDeliveryCanceled(raw: String) {
        val event = objectMapper.readValue(raw, DeliveryCanceledEvent::class.java)
        val orderId = UUID.fromString(event.orderId)
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        statusMachine.validateTransition(order.status, OrderStatus.CANCELLED)
        order.status = OrderStatus.CANCELLED
        orderRepository.save(order)
    }

    @KafkaListener(topics = ["\${app.kafka.topic.rollback-successfully}"])
    @Transactional
    fun onRollbackSuccessfullyEvent(raw: String) {
        val event = objectMapper.readValue(raw, RollbackSuccessfullyEvent::class.java)
        if (processedEventRepository.existsByEventId(event.eventId)) return

        val order = orderRepository.findById(event.orderId)
            .orElseThrow { OrderNotFoundException(event.orderId) }
        statusMachine.validateTransition(order.status, OrderStatus.CANCELLED)
        order.status = OrderStatus.CANCELLED
        orderRepository.save(order)
        val payload = CancelledEvent(orderId = order.id, reason = "payment_failed")
        saveOutbox("order-cancelled-event", order.id.toString(), payload)
        processedEventRepository.save(ProcessedEventEntity(event.eventId, event.orderId, "rollback-successfully-event"))
    }

    private fun saveOutbox(topic: String, aggregateId: String, payload: Any) {
        val json = objectMapper.writeValueAsString(payload)
        outboxRepository.save(OutboxEntity(
            aggregateId = aggregateId,
            eventType = topic,
            payload = json
        ))
    }

    data class PlacedEvent(val orderId: UUID)
    data class CancelledEvent(val orderId: UUID, val reason: String)
}
