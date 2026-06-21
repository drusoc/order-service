package by.java.enterprise.orderservice.outbox

import tools.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class OutboxPoller(
    private val outboxRepository: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    @Scheduled(fixedDelayString = "\${app.outbox.poll-interval:1000}")
    @Transactional
    fun publishPendingEvents() {
        val cutoff = LocalDateTime.now().minusSeconds(5)
        val pending = outboxRepository.findByPublishedAtIsNullAndCreatedAtBefore(cutoff)
        for (event in pending) {
            try {
                kafkaTemplate.send(event.eventType, event.aggregateId, event.payload).get()
                event.publishedAt = LocalDateTime.now()
                outboxRepository.save(event)
            } catch (e: Exception) {
                log.error("Failed to publish outbox event id={}, topic={}", event.id, event.eventType, e)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OutboxPoller::class.java)
    }
}
