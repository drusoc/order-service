package by.java.enterprise.orderservice.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class OrderEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    fun send(event: OrderCreatedEvent) {
        try {
            val json = objectMapper.writeValueAsString(event)
            kafkaTemplate.send("order.events", event.id.toString(), json)
            log.info("Order-created event sent to topic order.events: orderId={}", event.id)
        } catch (e: Exception) {
            log.error("Failed to send order-created event: orderId={}", event.id, e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OrderEventProducer::class.java)
    }
}
