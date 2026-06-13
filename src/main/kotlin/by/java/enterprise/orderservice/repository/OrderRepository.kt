package by.java.enterprise.orderservice.repository

import by.java.enterprise.orderservice.entity.OrderEntity
import by.java.enterprise.orderservice.entity.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<OrderEntity, UUID> {

    fun findByUserId(userId: UUID, pageable: Pageable): Page<OrderEntity>

    fun findByUserIdAndStatus(userId: UUID, status: OrderStatus, pageable: Pageable): Page<OrderEntity>

    fun findByIdAndUserId(id: UUID, userId: UUID): OrderEntity?
}
