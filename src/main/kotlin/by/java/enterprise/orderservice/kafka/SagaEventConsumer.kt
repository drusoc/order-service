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
            val payload = PlacedEvent(orderId = order.id)
            saveOutbox("order-placed-event", orderId.toString(), payload)
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
