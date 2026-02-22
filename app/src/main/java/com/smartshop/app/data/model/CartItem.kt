package com.smartshop.app.data.model

data class CartItem (
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 1
) {
    // Computed property - calculated automatically, not stored
    val totalPrice: Double get() = price * quantity
}