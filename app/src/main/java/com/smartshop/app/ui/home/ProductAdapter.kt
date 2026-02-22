package com.smartshop.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smartshop.app.R
import com.smartshop.app.data.model.Product
import com.smartshop.app.databinding.ItemProductBinding
import com.smartshop.app.utils.toCurrencyString

class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onAddToCart: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback) {

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {

                productName.text = product.name
                productPrice.text = product.price.toCurrencyString()
                productCategory.text = product.category

                // Stock indicator
                if (product.stock > 0) {
                    stockBadge.text = "In Stock"
                    stockBadge.setTextColor(
                        root.context.getColor(android.R.color.holo_green_dark)
                    )
                } else {
                    stockBadge.text = "Out of Stock"
                    stockBadge.setTextColor(
                        root.context.getColor(android.R.color.holo_red_dark)
                    )
                }

                // Load image with Glide
                Glide.with(productImage.context)
                    .load(product.imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(productImage)

                // Disable add to cart if out of stock
                addToCartButton.isEnabled = product.stock > 0

                // Click listeners
                root.setOnClickListener { onProductClick(product) }
                addToCartButton.setOnClickListener { onAddToCart(product) }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) = holder.bind(getItem(position))

    // DiffCallback — only re-renders items that actually changed
    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(
            oldItem: Product,
            newItem: Product
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Product,
            newItem: Product
        ) = oldItem == newItem
    }
}