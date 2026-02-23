package com.smartshop.app.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshop.app.data.model.Product
import com.smartshop.app.data.repository.ProductRepository
import com.smartshop.app.data.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceCompareViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<Resource<List<Product>>>(Resource.Loading)
    val products: StateFlow<Resource<List<Product>>> = _products

    fun loadComparisons(productName: String) {
        viewModelScope.launch {
            repository.getProductsByName(productName).collect {
                _products.value = it
            }
        }
    }
}