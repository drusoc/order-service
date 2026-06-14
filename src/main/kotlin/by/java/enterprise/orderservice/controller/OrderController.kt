package by.java.enterprise.orderservice.controller

import by.java.enterprise.orderservice.dto.request.ChangeStatusRequest
import by.java.enterprise.orderservice.dto.request.CreateOrderRequest
import by.java.enterprise.orderservice.dto.response.OrderHistoryResponse
import by.java.enterprise.orderservice.dto.response.OrderResponse
import by.java.enterprise.orderservice.entity.OrderStatus
import by.java.enterprise.orderservice.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping
    fun createOrder(
        @RequestHeader("current-user-id") userId: UUID,
        @Valid @RequestBody request: CreateOrderRequest
    ): ResponseEntity<OrderResponse> {
        val order = orderService.createOrder(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(order)
    }

    @GetMapping
    fun getOrderHistory(
        @RequestHeader("current-user-id") userId: UUID,
        @RequestParam(required = false) status: OrderStatus?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<OrderHistoryResponse> {
        val history = orderService.getOrderHistory(userId, status, page, size)
        return ResponseEntity.ok(history)
    }

    @GetMapping("/{id}")
    fun getOrderDetails(
        @PathVariable id: UUID
    ): ResponseEntity<OrderResponse> {
        val order = orderService.getOrderDetails(id)
        return ResponseEntity.ok(order)
    }

    @PutMapping("/{id}/status")
    fun changeOrderStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChangeStatusRequest
    ): ResponseEntity<OrderResponse> {
        val order = orderService.changeOrderStatus(id, request)
        return ResponseEntity.ok(order)
    }
}
