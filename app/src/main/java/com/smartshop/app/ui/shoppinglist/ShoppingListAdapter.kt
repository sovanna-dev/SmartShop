package com.smartshop.app.ui.shoppinglist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartshop.app.data.model.ShoppingList
import com.smartshop.app.databinding.ItemShoppingListBinding

class ShoppingListAdapter(
    private val onListClick: (ShoppingList) -> Unit,
    private val onDeleteClick: (ShoppingList) -> Unit
) : ListAdapter<ShoppingList, ShoppingListAdapter.ListViewHolder>(DiffCallback()) {

    inner class ListViewHolder(
        private val binding: ItemShoppingListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(list: ShoppingList) {
            binding.listName.text = list.name
            binding.itemCount.text = "${list.totalItems} items"
            binding.progressText.text = "${list.checkedItems}/${list.totalItems} done"

            // Progress bar
            if (list.totalItems > 0) {
                binding.listProgress.max = list.totalItems
                binding.listProgress.progress = list.checkedItems
            }

            binding.root.setOnClickListener { onListClick(list) }
            binding.deleteButton.setOnClickListener { onDeleteClick(list) }
        }
    }

    class DiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<ShoppingList>() {
        override fun areItemsTheSame(old: ShoppingList, new: ShoppingList) = old.id == new.id
        override fun areContentsTheSame(old: ShoppingList, new: ShoppingList) = old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemShoppingListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) =
        holder.bind(getItem(position))
}