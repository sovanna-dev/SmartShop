package com.smartshop.app.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshop.app.data.model.Order
import com.smartshop.app.data.repository.OrderRepository
import com.smartshop.app.data.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CategorySpending(
    val category: String,
    val total: Double,
    val percentage: Int
)

data class SpendingStats(
    val totalThisMonth: Double,
    val totalAllTime: Double,
    val totalOrders: Int,
    val averageOrderValue: Double,
    val categoryBreakdown: List<CategorySpending>,
    val recentOrders: List<Order>
)

@HiltViewModel
class SpendingTrackerViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    private val _stats = MutableStateFlow<Resource<SpendingStats>>(Resource.Loading)
    val stats: StateFlow<Resource<SpendingStats>> = _stats

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getUserOrders().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _stats.value = Resource.Loading
                    is Resource.Error -> _stats.value = Resource.Error(resource.message)
                    is Resource.Success -> {
                        _stats.value = Resource.Success(calculateStats(resource.data))
                    }
                }
            }
        }
    }

    private fun calculateStats(orders: List<Order>): SpendingStats {
        if (orders.isEmpty()) {
            return SpendingStats(0.0, 0.0, 0, 0.0, emptyList(), emptyList())
        }

        // This month orders
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val thisMonthOrders = orders.filter { order ->
            val orderCal = Calendar.getInstance()
            order.timestamp?.let { orderCal.time = it.toDate() }
            orderCal.get(Calendar.MONTH) == currentMonth &&
                    orderCal.get(Calendar.YEAR) == currentYear
        }

        val totalThisMonth = thisMonthOrders.sumOf { it.totalPrice }
        val totalAllTime = orders.sumOf { it.totalPrice }
        val avgOrder = if (orders.isNotEmpty()) totalAllTime / orders.size else 0.0

        // Category breakdown from all orders
        val categoryTotals = mutableMapOf<String, Double>()
        orders.forEach { order ->
            order.items.forEach { item ->
                val cat = item.category.ifEmpty { "Other" }
                categoryTotals[cat] = (categoryTotals[cat] ?: 0.0) + item.totalPrice
            }
        }

        val maxCategoryTotal = categoryTotals.values.maxOrNull() ?: 1.0
        val categoryBreakdown = categoryTotals.entries
            .sortedByDescending { it.value }
            .map { (cat, total) ->
                CategorySpending(
                    category = cat,
                    total = total,
                    percentage = ((total / maxCategoryTotal) * 100).toInt()
                )
            }

        return SpendingStats(
            totalThisMonth = totalThisMonth,
            totalAllTime = totalAllTime,
            totalOrders = orders.size,
            averageOrderValue = avgOrder,
            categoryBreakdown = categoryBreakdown,
            recentOrders = orders.take(5)
        )
    }
}