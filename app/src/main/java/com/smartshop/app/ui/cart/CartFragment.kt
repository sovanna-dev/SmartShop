package com.smartshop.app.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.smartshop.app.R
import com.smartshop.app.data.model.Resource
import com.smartshop.app.databinding.FragmentCartBinding
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.toCurrencyString
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToolbar()
        setupCheckoutButton()
        observeCart()
        observeTotal()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onIncrease = { productId ->
                viewModel.increaseQuantity(productId)
            },
            onDecrease = { productId ->
                viewModel.decreaseQuantity(productId)
            },
            onRemove = { productId ->
                viewModel.removeItem(productId)
            }
        )

        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun setupToolbar() {

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCheckoutButton() {
        binding.checkoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_cart_to_checkout)
        }
    }

    private fun observeCart() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cartItems.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.emptyContainer.gone()
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        val items = state.data
                        if (items.isEmpty()) {
                            binding.emptyContainer.visible()
                            binding.cartRecyclerView.gone()
                            binding.checkoutButton.isEnabled = false
                            binding.itemCountBadge.text = ""
                        } else {
                            binding.emptyContainer.gone()
                            binding.cartRecyclerView.visible()
                            binding.checkoutButton.isEnabled = true
                            val totalItems = items.sumOf { it.quantity }
                            binding.itemCountBadge.text = "$totalItems items"
                            cartAdapter.submitList(items)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.emptyContainer.visible()
                    }
                }
            }
        }
    }

    private fun observeTotal() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cartTotal.collectLatest { total ->
                binding.totalAmount.text = total.toCurrencyString()
                binding.subtotalValue.text = total.toCurrencyString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}