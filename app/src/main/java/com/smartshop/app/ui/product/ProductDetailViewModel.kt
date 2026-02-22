package com.smartshop.app.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshop.app.data.model.Product
import com.smartshop.app.data.model.Resource
import com.smartshop.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _product = MutableStateFlow<Resource<Product>>(Resource.Loading)
    val product = _product.asStateFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _product.value = Resource.Loading
            _product.value = productRepository.getProductById(productId)
        }
    }
}