package by.java.enterprise.orderservice.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "outbox")
class OutboxEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "aggregate_id", nullable = false)
    val aggregateId: String,

    @Column(name = "event_type", nullable = false)
    val eventType: String,

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    val payload: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "published_at")
    var publishedAt: LocalDateTime? = null
)
