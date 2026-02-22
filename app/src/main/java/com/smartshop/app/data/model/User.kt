package com.smartshop.app.data.model



data class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: Address = Address()
)

data class Address(
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = ""
)