package com.smartshop.app.data.model

import com.google.firebase.Timestamp

data class ShoppingListItem(
    val id: String = "",
    val productId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val imageUrl: String = "",
    val isChecked: Boolean = false,
    val addedAt: Timestamp? = null
)