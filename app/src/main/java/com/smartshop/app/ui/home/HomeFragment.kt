package com.smartshop.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartshop.app.R
import com.smartshop.app.data.model.Resource
import com.smartshop.app.databinding.FragmentHomeBinding
import com.smartshop.app.ui.auth.AuthViewModel
import com.smartshop.app.ui.cart.CartViewModel
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.facebook.shimmer.ShimmerFrameLayout
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private val cartViewModel: CartViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProductRecyclerView()
        setupCategoryRecyclerView()
        setupSearch()
        setupCartButton()
        setupLogoutButton()
        observeProducts()
        observeCategories()
        observeFilteredProducts()

        binding.homeTitle.setOnLongClickListener {
            findNavController().navigate(R.id.action_home_to_orders)
            true
        }

    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    authViewModel.logout()
                    findNavController().navigate(
                        R.id.action_splash_to_login
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupProductRecyclerView() {
        productAdapter = ProductAdapter(
            onProductClick = { product ->
                // Navigate to product detail with product ID
                val action = HomeFragmentDirections
                    .actionHomeToProductDetail(product.id)
                findNavController().navigate(action)
            },
            onAddToCart = { product ->
                cartViewModel.addToCart(product)
                com.google.android.material.snackbar.Snackbar
                    .make(
                        binding.root,
                        "${product.name} added to cart",
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
            }
        )

        binding.productsRecyclerView.apply {
            // 2-column grid
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            viewModel.setCategory(category)
        }

        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = categoryAdapter
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }
    }


    private fun setupCartButton() {
        binding.cartButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_cart)
        }
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        // Show shimmer, hide real list
                        binding.shimmerView.visible()
                        (binding.shimmerView as com.facebook.shimmer.ShimmerFrameLayout)
                            .startShimmer()
                        binding.productsRecyclerView.gone()
                        binding.emptyText.gone()
                    }
                    is Resource.Success -> {
                        // Stop shimmer, show real list
                        (binding.shimmerView as com.facebook.shimmer.ShimmerFrameLayout)
                            .stopShimmer()
                        binding.shimmerView.gone()
                        binding.productsRecyclerView.visible()
                    }
                    is Resource.Error -> {
                        (binding.shimmerView as com.facebook.shimmer.ShimmerFrameLayout)
                            .stopShimmer()
                        binding.shimmerView.gone()
                        binding.emptyText.visible()
                        binding.emptyText.text = state.message
                    }
                }
            }
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collectLatest { categories ->
                categoryAdapter.submitList(categories)
            }
        }
    }

    private fun observeFilteredProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredProducts.collectLatest { products ->
                productAdapter.submitList(products)

                // Show empty state if no results
                if (products.isEmpty()) {
                    binding.emptyText.visible()
                    binding.emptyText.text = "No products found"
                } else {
                    binding.emptyText.gone()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}