package com.smartshop.app.ui.shoppinglist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smartshop.app.data.model.ShoppingListItem
import com.smartshop.app.databinding.ItemListItemBinding
import com.smartshop.app.utils.toCurrencyString

class ListItemAdapter(
    private val onCheckedChange: (ShoppingListItem, Boolean) -> Unit,
    private val onRemoveClick: (ShoppingListItem) -> Unit
) : ListAdapter<ShoppingListItem, ListItemAdapter.ItemViewHolder>(DiffCallback()) {

    inner class ItemViewHolder(
        private val binding: ItemListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShoppingListItem) {
            binding.itemName.text = item.name
            binding.itemPrice.text = item.price.toCurrencyString()
            binding.itemQuantity.text = "Qty: ${item.quantity}"
            binding.itemCheckbox.isChecked = item.isChecked

            // Strike through if checked
            if (item.isChecked) {
                binding.itemName.paintFlags =
                    binding.itemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.root.alpha = 0.6f
            } else {
                binding.itemName.paintFlags =
                    binding.itemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.root.alpha = 1.0f
            }

            Glide.with(binding.root)
                .load(item.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.itemImage)

            binding.itemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(item, isChecked)
            }

            binding.removeButton.setOnClickListener { onRemoveClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ShoppingListItem>() {
        override fun areItemsTheSame(old: ShoppingListItem, new: ShoppingListItem) =
            old.id == new.id
        override fun areContentsTheSame(old: ShoppingListItem, new: ShoppingListItem) =
            old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))
}