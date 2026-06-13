package by.java.enterprise.orderservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "order_items")
class OrderItemEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: OrderEntity = null as OrderEntity,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "price_at_purchase", nullable = false)
    val priceAtPurchase: Double,

    @Column(name = "discount_at_purchase")
    val discountAtPurchase: Short = 0,

    @Column(name = "final_price_per_item", nullable = false)
    val finalPricePerItem: Double,

    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Column(name = "seller_name", nullable = false)
    val sellerName: String,

    @Column(name = "article", nullable = false)
    val article: String,

    @Column(name = "barcode", nullable = false)
    val barcode: String,

    @Column(name = "weight")
    val weight: Double = 0.0,

    @Column(name = "width")
    val width: Double = 0.0,

    @Column(name = "height")
    val height: Double = 0.0,

    @Column(name = "depth")
    val depth: Double = 0.0
)
