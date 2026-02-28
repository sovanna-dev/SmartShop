package com.smartshop.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshop.app.data.model.Product
import com.smartshop.app.data.model.Resource
import com.smartshop.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    // Raw products from Firestore
    private val _products = MutableStateFlow<Resource<List<Product>>>(Resource.Loading)
    val products = _products.asStateFlow()

    // Search query typed by user
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Selected category filter
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()
    private val _selectedStore = MutableStateFlow("All")
    val selectedStore: StateFlow<String> = _selectedStore

    fun setSelectedStore(store: String) {
        _selectedStore.value = store
    }
    // Filtered list — auto updates when products, search, or category changes
    val filteredProducts = combine(
        _products,
        _searchQuery,
        _selectedCategory,
        _selectedStore
    ) { resource, query, category, store ->
        val list = (resource as? Resource.Success)?.data ?: return@combine emptyList()
        list.filter { product ->
            val matchesSearch = query.isEmpty() ||
                    product.name.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" ||
                    product.category == category
            val matchesStore = store == "All" ||
                    product.store == store
            matchesSearch && matchesCategory && matchesStore
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val stores = _products.map { resource ->
        val list = (resource as? Resource.Success)?.data
            ?: return@map listOf("All")
        val storeList = list.map { it.store }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
        listOf("All") + storeList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("All")
    )
    // All unique categories from products
    val categories = _products.map { resource ->
        val list = (resource as? Resource.Success)?.data
            ?: return@map listOf("All")
        val cats = list.map { it.category }.distinct().sorted()
        listOf("All") + cats
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("All")
    )

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProducts().collect { result ->
                _products.value = result
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }
}