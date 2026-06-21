package by.java.enterprise.orderservice.outbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface OutboxRepository : JpaRepository<OutboxEntity, UUID> {

    fun findByPublishedAtIsNullAndCreatedAtBefore(cutoff: LocalDateTime): List<OutboxEntity>
}
