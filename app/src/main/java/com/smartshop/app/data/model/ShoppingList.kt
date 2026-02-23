package com.smartshop.app.data.model

import com.google.firebase.Timestamp

data class ShoppingList(
    val id: String = "",
    val name: String = "",
    val createdAt: Timestamp? = null,
    val totalItems: Int = 0,
    val checkedItems: Int = 0
)