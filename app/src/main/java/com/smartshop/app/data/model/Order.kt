package com.smartshop.app.data.model

import com.google.firebase.Timestamp

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val shippingAddress: Address = Address(),
    val status: OrderStatus = OrderStatus.PENDING,
    val timestamp: Timestamp = Timestamp.now()
)

enum class OrderStatus{
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED

}