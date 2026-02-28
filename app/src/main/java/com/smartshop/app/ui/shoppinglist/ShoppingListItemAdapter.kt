package com.smartshop.app.ui.shoppinglist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smartshop.app.data.model.ShoppingListItem
import com.smartshop.app.databinding.ItemShoppingListItemBinding

import com.smartshop.app.utils.toCurrencyString

class ShoppingListItemAdapter(
    private val onCheckedChange: (ShoppingListItem, Boolean) -> Unit,
    private val onDeleteClick: (ShoppingListItem) -> Unit
) : ListAdapter<ShoppingListItem, ShoppingListItemAdapter.ItemViewHolder>(DiffCallback()) {

    inner class ItemViewHolder(
        private val binding: ItemShoppingListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShoppingListItem) {
            binding.itemName.text = item.name
            binding.itemPrice.text = item.price.toCurrencyString()
            binding.itemQty.text = "Qty: ${item.quantity}"

            // Load image
            Glide.with(binding.root)
                .load(item.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.itemImage)

            // Strikethrough if checked
            if (item.isChecked) {
                binding.itemName.paintFlags =
                    binding.itemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.itemPrice.paintFlags =
                    binding.itemPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.root.alpha = 0.5f
            } else {
                binding.itemName.paintFlags =
                    binding.itemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.itemPrice.paintFlags =
                    binding.itemPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.root.alpha = 1f
            }

            // Prevent listener firing during bind
            binding.checkbox.setOnCheckedChangeListener(null)
            binding.checkbox.isChecked = item.isChecked
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(item, isChecked)
            }

            binding.deleteButton.setOnClickListener { onDeleteClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ShoppingListItem>() {
        override fun areItemsTheSame(old: ShoppingListItem, new: ShoppingListItem) =
            old.id == new.id
        override fun areContentsTheSame(old: ShoppingListItem, new: ShoppingListItem) =
            old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemShoppingListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    // Unchecked items first, checked items at bottom
    fun submitSorted(items: List<ShoppingListItem>) {
        submitList(items.sortedBy { it.isChecked })
    }
}