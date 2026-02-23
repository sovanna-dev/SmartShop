package com.smartshop.app.ui.shoppinglist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smartshop.app.data.model.Product
import com.smartshop.app.databinding.ItemProductPickerBinding
import com.smartshop.app.utils.toCurrencyString

class ProductPickerAdapter(
    private val onAddClick: (Product) -> Unit
) : ListAdapter<Product, ProductPickerAdapter.PickerViewHolder>(DiffCallback()) {

    inner class PickerViewHolder(
        private val binding: ItemProductPickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.productName.text = product.name
            binding.productCategory.text = product.category
            binding.productPrice.text = product.price.toCurrencyString()

            Glide.with(binding.root)
                .load(product.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.productImage)

            binding.addButton.setOnClickListener { onAddClick(product) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
        override fun areContentsTheSame(old: Product, new: Product) = old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerViewHolder {
        val binding = ItemProductPickerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PickerViewHolder, position: Int) =
        holder.bind(getItem(position))
}