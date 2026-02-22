package com.smartshop.app.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshop.app.data.model.Address
import com.smartshop.app.data.model.CartItem
import com.smartshop.app.data.model.Resource
import com.smartshop.app.data.repository.CartRepository
import com.smartshop.app.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<Resource<String>?>(null)
    val orderState = _orderState.asStateFlow()

    fun placeOrder(
        items: List<CartItem>,
        totalPrice: Double,
        shippingAddress: Address
    ) {
        viewModelScope.launch {
            _orderState.value = Resource.Loading

            // Step 1 — Save order to Firestore
            val result = orderRepository.placeOrder(
                items, totalPrice, shippingAddress
            )

            if (result is Resource.Success) {
                // Step 2 — Clear the cart after successful order
                cartRepository.clearCart()
            }

            _orderState.value = result
        }
    }

    fun resetOrderState() {
        _orderState.value = null
    }
}