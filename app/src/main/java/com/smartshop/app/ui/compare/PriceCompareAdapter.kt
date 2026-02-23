package com.smartshop.app.ui.compare

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartshop.app.data.model.Product
import com.smartshop.app.databinding.ItemPriceCompareBinding
import com.smartshop.app.utils.toCurrencyString

class PriceCompareAdapter(
    private val onAddToCart: (Product) -> Unit
) : ListAdapter<Product, PriceCompareAdapter.CompareViewHolder>(DiffCallback()) {

    private var cheapestPrice: Double = Double.MAX_VALUE

    fun submitListWithCheapest(list: List<Product>) {
        cheapestPrice = list.minOfOrNull { it.price } ?: Double.MAX_VALUE
        submitList(list)
    }

    inner class CompareViewHolder(
        private val binding: ItemPriceCompareBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.storeName.text = product.store
            binding.storePrice.text = product.price.toCurrencyString()

            // Highlight cheapest
            val isCheapest = product.price == cheapestPrice
            if (isCheapest) {
                binding.cheapestBadge.visibility = android.view.View.VISIBLE
                binding.storePrice.setTextColor(
                    android.graphics.Color.parseColor("#E8420A")
                )
                binding.root.strokeColor =
                    android.graphics.Color.parseColor("#E8420A")
                binding.root.strokeWidth = 3
            } else {
                binding.cheapestBadge.visibility = android.view.View.GONE
                binding.storePrice.setTextColor(
                    android.graphics.Color.parseColor("#1A0A00")
                )
                binding.root.strokeColor =
                    android.graphics.Color.parseColor("#F0D5CC")
                binding.root.strokeWidth = 1
            }

            // Price difference vs cheapest
            val diff = product.price - cheapestPrice
            if (diff > 0) {
                binding.priceDiff.visibility = android.view.View.VISIBLE
                binding.priceDiff.text = "+${diff.toCurrencyString()} more"
            } else {
                binding.priceDiff.visibility = android.view.View.GONE
            }

            binding.addToCartButton.setOnClickListener { onAddToCart(product) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
        override fun areContentsTheSame(old: Product, new: Product) = old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompareViewHolder {
        val binding = ItemPriceCompareBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CompareViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompareViewHolder, position: Int) =
        holder.bind(getItem(position))
}