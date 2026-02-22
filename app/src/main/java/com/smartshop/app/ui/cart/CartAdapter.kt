package com.smartshop.app.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smartshop.app.R
import com.smartshop.app.data.model.CartItem
import com.smartshop.app.databinding.ItemCartBinding
import com.smartshop.app.utils.toCurrencyString

class CartAdapter(
    private val onIncrease: (String) -> Unit,
    private val onDecrease: (String) -> Unit,
    private val onRemove: (String) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback) {

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.apply {
                cartItemName.text = item.name
                cartItemPrice.text = item.price.toCurrencyString()
                quantityText.text = item.quantity.toString()
                cartItemTotal.text = "Total: ${item.totalPrice.toCurrencyString()}"

                Glide.with(cartItemImage.context)
                    .load(item.imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(cartItemImage)

                increaseButton.setOnClickListener {
                    onIncrease(item.productId)
                }

                decreaseButton.setOnClickListener {
                    onDecrease(item.productId)
                }

                removeButton.setOnClickListener {
                    onRemove(item.productId)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int
    ) = holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(
            old: CartItem,
            new: CartItem
        ) = old.productId == new.productId

        override fun areContentsTheSame(
            old: CartItem,
            new: CartItem
        ) = old == new
    }
}