package com.smartshop.app.data.model

data class Product(

    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val stock: Int = 0,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val store: String = ""
)