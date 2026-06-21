package by.java.enterprise.orderservice.outbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedEventRepository : JpaRepository<ProcessedEventEntity, String> {

    fun existsByEventId(eventId: String): Boolean
}
