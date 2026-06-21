package by.java.enterprise.orderservice.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus = OrderStatus.CREATED,

    @Column(name = "total_price", nullable = false)
    var totalPrice: Double = 0.0,

    @Column(name = "total_weight", nullable = false)
    var totalWeight: Double = 0.0,

    @Column(name = "delivery_address_id", nullable = false)
    val deliveryAddressId: UUID,

    @Column(name = "recipient_name")
    var recipientName: String? = null,

    @Column(name = "recipient_phone")
    var recipientPhone: String? = null,

    @Column(name = "pickup_point_code")
    var pickupPointCode: String? = null,

    @Column(name = "payment_transaction_id")
    var paymentTransactionId: UUID? = null,

    @Column(name = "delivery_id")
    var deliveryId: UUID? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val items: MutableList<OrderItemEntity> = mutableListOf()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
