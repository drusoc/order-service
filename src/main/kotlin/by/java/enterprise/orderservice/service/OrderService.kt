package by.java.enterprise.orderservice.service

import by.java.enterprise.orderservice.dto.request.ChangeStatusRequest
import by.java.enterprise.orderservice.dto.request.CreateOrderRequest
import by.java.enterprise.orderservice.dto.response.OrderHistoryResponse
import by.java.enterprise.orderservice.dto.response.OrderItemResponse
import by.java.enterprise.orderservice.dto.response.OrderResponse
import by.java.enterprise.orderservice.dto.response.OrderSummaryResponse
import by.java.enterprise.orderservice.entity.OrderEntity
import by.java.enterprise.orderservice.entity.OrderItemEntity
import by.java.enterprise.orderservice.entity.OrderStatus
import by.java.enterprise.orderservice.exception.OrderNotFoundException
import by.java.enterprise.orderservice.kafka.ReserveProductEvent
import by.java.enterprise.orderservice.kafka.ReserveProductItem
import by.java.enterprise.orderservice.outbox.OutboxEntity
import by.java.enterprise.orderservice.outbox.OutboxRepository
import by.java.enterprise.orderservice.repository.OrderRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.util.*

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val totalCalculator: OrderTotalCalculator,
    private val statusMachine: OrderStatusMachine,
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper
) {

    fun createOrder(userId: UUID, request: CreateOrderRequest): OrderResponse {
        val totalPrice = totalCalculator.calculateTotalPrice(request.items)
        val totalWeight = totalCalculator.calculateTotalWeight(request.items)

        val order = OrderEntity(
            userId = userId,
            deliveryAddressId = request.deliveryAddressId,
            totalPrice = totalPrice,
            totalWeight = totalWeight
        )

        request.items.forEach { item ->
            order.items.add(
                OrderItemEntity(
                    order = order,
                    productId = item.productId,
                    title = item.title,
                    priceAtPurchase = item.priceAtPurchase,
                    discountAtPurchase = item.discountAtPurchase,
                    finalPricePerItem = item.finalPricePerItem,
                    quantity = item.quantity,
                    sellerName = item.sellerName,
                    article = item.article,
                    barcode = item.barcode,
                    weight = item.weight,
                    width = item.width,
                    height = item.height,
                    depth = item.depth
                )
            )
        }

        val saved = orderRepository.save(order)
        publishOrderCreatedEvent(saved)
        return saved.toResponse()
    }

    fun getOrderHistory(
        userId: UUID,
        status: OrderStatus?,
        page: Int,
        size: Int
    ): OrderHistoryResponse {
        val pageRequest = PageRequest.of(page, size)
        val orderPage = if (status != null) {
            orderRepository.findByUserIdAndStatus(userId, status, pageRequest)
        } else {
            orderRepository.findByUserId(userId, pageRequest)
        }

        return OrderHistoryResponse(
            orders = orderPage.content.map { it.toSummary() },
            totalCount = orderPage.totalElements,
            page = orderPage.number,
            size = orderPage.size
        )
    }

    fun getOrderDetails(orderId: UUID): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }
        return order.toResponse()
    }

    fun changeOrderStatus(orderId: UUID, request: ChangeStatusRequest): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        statusMachine.validateTransition(order.status, request.status)
        order.status = request.status
        return order.toResponse()
    }

    private fun publishOrderCreatedEvent(order: OrderEntity) {
        val reserveEvent = ReserveProductEvent(
            eventId = UUID.randomUUID().toString(),
            orderId = order.id,
            items = order.items.map {
                ReserveProductItem(productId = it.productId, quantity = it.quantity)
            }
        )
        val json = objectMapper.writeValueAsString(reserveEvent)
        outboxRepository.save(OutboxEntity(
            aggregateId = order.id.toString(),
            eventType = "reserve-product-event",
            payload = json
        ))
    }
}

private fun OrderEntity.toResponse() = OrderResponse(
    id = id, userId = userId, status = status,
    totalPrice = totalPrice, totalWeight = totalWeight,
    deliveryAddressId = deliveryAddressId,
    paymentTransactionId = paymentTransactionId,
    deliveryId = deliveryId,
    createdAt = createdAt, updatedAt = updatedAt,
    items = items.map { it.toResponse() }
)

private fun OrderItemEntity.toResponse() = OrderItemResponse(
    id = id, productId = productId, title = title,
    priceAtPurchase = priceAtPurchase, discountAtPurchase = discountAtPurchase,
    finalPricePerItem = finalPricePerItem, quantity = quantity,
    sellerName = sellerName, article = article, barcode = barcode,
    weight = weight, width = width, height = height, depth = depth
)

private fun OrderEntity.toSummary() = OrderSummaryResponse(
    id = id, userId = userId, status = status,
    totalPrice = totalPrice, totalWeight = totalWeight,
    deliveryAddressId = deliveryAddressId,
    createdAt = createdAt, updatedAt = updatedAt
)
