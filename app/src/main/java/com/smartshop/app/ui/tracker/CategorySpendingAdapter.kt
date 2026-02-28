package com.smartshop.app.ui.tracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartshop.app.databinding.ItemCategorySpendingBinding
import com.smartshop.app.utils.toCurrencyString

class CategorySpendingAdapter :
    ListAdapter<CategorySpending, CategorySpendingAdapter.SpendingViewHolder>(DiffCallback()) {

    inner class SpendingViewHolder(
        private val binding: ItemCategorySpendingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategorySpending) {
            binding.categoryName.text = item.category
            binding.categoryTotal.text = item.total.toCurrencyString()
            binding.categoryProgress.progress = item.percentage
            binding.percentageText.text = "${item.percentage}%"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CategorySpending>() {
        override fun areItemsTheSame(old: CategorySpending, new: CategorySpending) =
            old.category == new.category
        override fun areContentsTheSame(old: CategorySpending, new: CategorySpending) =
            old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpendingViewHolder {
        val binding = ItemCategorySpendingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SpendingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpendingViewHolder, position: Int) =
        holder.bind(getItem(position))
}