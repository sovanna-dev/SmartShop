package com.smartshop.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.smartshop.app.R

class CategoryAdapter(
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categories = mutableListOf<String>()
    private var selectedPosition = 0

    inner class CategoryViewHolder(
        val chip: Chip
    ) : RecyclerView.ViewHolder(chip)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_chip, parent, false) as Chip
        return CategoryViewHolder(chip)
    }

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {
        val category = categories[position]
        holder.chip.text = category
        holder.chip.isChecked = position == selectedPosition
        holder.chip.setTextColor(
            if (position == selectedPosition)
                android.graphics.Color.WHITE
            else
                android.graphics.Color.parseColor("#E8420A")
        )

        holder.chip.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onCategorySelected(category)
        }
    }

    override fun getItemCount() = categories.size

    fun submitList(newCategories: List<String>) {
        categories = newCategories.toMutableList()
        notifyDataSetChanged()
    }


}
