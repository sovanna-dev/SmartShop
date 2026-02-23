package com.smartshop.app.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.smartshop.app.R
import com.smartshop.app.data.model.Product
import com.smartshop.app.data.model.Resource
import com.smartshop.app.databinding.FragmentProductDetailBinding
import com.smartshop.app.ui.cart.CartViewModel
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.toCurrencyString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.smartshop.app.ui.product.ProductDetailFragmentDirections

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductDetailViewModel by viewModels()

    // Safe Args — automatically reads productId from navigation
    private val args: ProductDetailFragmentArgs by navArgs()
    private val cartViewModel: CartViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load product using the ID passed from Home
        viewModel.loadProduct(args.productId)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        observeProduct()

        binding.addToCartButton.setOnClickListener {
            val product = (viewModel.product.value as? Resource.Success)?.data
                ?: return@setOnClickListener
            cartViewModel.addToCart(product)
            binding.root.showSnackbar("${product.name} added to cart")
        }

        binding.comparePricesButton.setOnClickListener {
            val product = (viewModel.product.value as? Resource.Success)?.data
                ?: return@setOnClickListener
            val action = ProductDetailFragmentDirections
                .actionProductDetailToPriceCompare(product.name)
            findNavController().navigate(action)
        }
    }

    private fun observeProduct() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.product.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> showLoading(true)
                    is Resource.Success -> {
                        showLoading(false)
                        bindProduct(state.data)
                    }
                    is Resource.Error -> {
                        showLoading(false)
                        binding.productName.text = state.message
                    }
                }
            }
        }
    }

    private fun bindProduct(product: Product) {
        binding.apply {
            productName.text = product.name
            productPrice.text = product.price.toCurrencyString()
            productCategory.text = product.category
            productDescription.text = product.description
            ratingBar.rating = product.rating
            ratingText.text = "${product.rating} (${product.reviewCount} reviews)"

            // Stock status
            if (product.stock > 0) {
                stockStatus.text = "In Stock (${product.stock})"
                stockStatus.setTextColor(
                    requireContext().getColor(android.R.color.holo_green_dark)
                )
            } else {
                stockStatus.text = "Out of Stock"
                stockStatus.setTextColor(
                    requireContext().getColor(android.R.color.holo_red_dark)
                )
            }

            // Load image
            Glide.with(productImage.context)
                .load(product.imageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .into(productImage)
        }
    }

    private fun showLoading(loading: Boolean) {
        // Simple loading state — name shows loading text
        if (loading) binding.productName.text = "Loading..."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}