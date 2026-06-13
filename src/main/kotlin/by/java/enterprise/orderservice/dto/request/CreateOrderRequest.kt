package by.java.enterprise.orderservice.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateOrderRequest(
    @field:NotNull
    val deliveryAddressId: UUID,

    @field:NotNull
    @field:Size(min = 1, message = "At least one item is required")
    val items: List<@Valid CreateOrderItemRequest>
)

data class CreateOrderItemRequest(
    @field:NotNull
    val productId: UUID,

    @field:NotBlank
    val title: String,

    @field:NotNull @field:Positive
    val priceAtPurchase: Double,

    @field:Max(100) @field:Min(0)
    val discountAtPurchase: Short = 0,

    val finalPricePerItem: Double,

    @field:NotNull @field:Positive
    val quantity: Int,

    @field:NotBlank
    val sellerName: String,

    @field:NotBlank
    val article: String,

    @field:NotBlank
    val barcode: String,

    val weight: Double = 0.0,
    val width: Double = 0.0,
    val height: Double = 0.0,
    val depth: Double = 0.0
)
