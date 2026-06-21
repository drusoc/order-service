package by.java.enterprise.orderservice.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "processed_events")
class ProcessedEventEntity(
    @Id
    @Column(name = "event_id", nullable = false)
    val eventId: String,

    @Column(name = "order_id", nullable = false)
    val orderId: UUID,

    @Column(name = "event_type", nullable = false)
    val eventType: String,

    @Column(name = "processed_at", nullable = false)
    val processedAt: LocalDateTime = LocalDateTime.now()
)
