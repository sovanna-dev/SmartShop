package com.smartshop.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshop.app.data.model.CartItem
import com.smartshop.app.data.model.Product
import com.smartshop.app.data.model.Resource
import com.smartshop.app.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartItems =
        MutableStateFlow<Resource<List<CartItem>>>(Resource.Loading)
    val cartItems = _cartItems.asStateFlow()

    // Auto-calculate total — updates whenever cart changes
    val cartTotal = _cartItems.map { resource ->
        (resource as? Resource.Success)
            ?.data
            ?.sumOf { it.totalPrice }
            ?: 0.0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Total item count for badge
    val cartItemCount = _cartItems.map { resource ->
        (resource as? Resource.Success)
            ?.data
            ?.sumOf { it.quantity }
            ?: 0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        loadCart()
    }

    private fun loadCart() {
        viewModelScope.launch {
            cartRepository.getCartItems().collect { result ->
                _cartItems.value = result
            }
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            val currentItems =
                (_cartItems.value as? Resource.Success)?.data
                    ?: emptyList()

            val existing = currentItems.find { it.productId == product.id }

            if (existing != null) {
                // Product already in cart — increase quantity
                cartRepository.updateQuantity(
                    product.id,
                    existing.quantity + 1
                )
            } else {
                // New item
                val item = CartItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    quantity = 1
                )
                cartRepository.addToCart(item)
            }
        }
    }

    fun increaseQuantity(productId: String) {
        viewModelScope.launch {
            val currentItems =
                (_cartItems.value as? Resource.Success)?.data
                    ?: return@launch
            val item = currentItems.find { it.productId == productId }
                ?: return@launch
            cartRepository.updateQuantity(productId, item.quantity + 1)
        }
    }

    fun decreaseQuantity(productId: String) {
        viewModelScope.launch {
            val currentItems =
                (_cartItems.value as? Resource.Success)?.data
                    ?: return@launch
            val item = currentItems.find { it.productId == productId }
                ?: return@launch
            cartRepository.updateQuantity(productId, item.quantity - 1)
        }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }
}