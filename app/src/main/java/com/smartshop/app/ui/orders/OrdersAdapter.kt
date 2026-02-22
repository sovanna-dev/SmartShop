package com.smartshop.app.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartshop.app.data.model.Order
import com.smartshop.app.data.model.OrderStatus
import com.smartshop.app.databinding.ItemOrderBinding
import com.smartshop.app.utils.toCurrencyString

class OrdersAdapter : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(DiffCallback) {

    inner class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.apply {
                // Show short order ID
                orderId.text = "Order #${order.id.take(8).uppercase()}"

                // Item count
                val totalItems = order.items.sumOf { it.quantity }
                orderItemCount.text = "$totalItems item(s)"

                // Total
                orderTotal.text = order.totalPrice.toCurrencyString()

                // Address
                val addr = order.shippingAddress
                orderAddress.text = "${addr.street}, ${addr.city}, ${addr.country}"

                // Status badge with color
                orderStatus.text = order.status.name

                val (bgColor, textColor) = when (order.status) {
                    OrderStatus.PENDING   -> "#FF9800" to "#FFFFFF"
                    OrderStatus.CONFIRMED -> "#2196F3" to "#FFFFFF"
                    OrderStatus.SHIPPED   -> "#9C27B0" to "#FFFFFF"
                    OrderStatus.DELIVERED -> "#4CAF50" to "#FFFFFF"
                    OrderStatus.CANCELLED -> "#F44336" to "#FFFFFF"
                }

                orderStatus.setBackgroundColor(
                    android.graphics.Color.parseColor(bgColor)
                )
                orderStatus.setTextColor(
                    android.graphics.Color.parseColor(textColor)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(old: Order, new: Order) = old.id == new.id
        override fun areContentsTheSame(old: Order, new: Order) = old == new
    }
}