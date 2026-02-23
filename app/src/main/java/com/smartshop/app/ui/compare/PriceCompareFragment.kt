package com.smartshop.app.ui.compare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartshop.app.databinding.FragmentPriceCompareBinding
import com.smartshop.app.ui.cart.CartViewModel
import com.smartshop.app.data.model.Resource
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.toCurrencyString
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PriceCompareFragment : Fragment() {

    private var _binding: FragmentPriceCompareBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PriceCompareViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val args: PriceCompareFragmentArgs by navArgs()
    private lateinit var compareAdapter: PriceCompareAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPriceCompareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.compareTitle.text = args.productName
        viewModel.loadComparisons(args.productName)
        setupRecyclerView()
        setupButtons()
        observeProducts()
    }

    private fun setupRecyclerView() {
        compareAdapter = PriceCompareAdapter { product ->
            cartViewModel.addToCart(product)
            binding.root.showSnackbar("Added from ${product.store} ✓")
        }
        binding.compareRecyclerView.apply {
            adapter = compareAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        val products = state.data

                        if (products.isEmpty()) return@collectLatest

                        compareAdapter.submitListWithCheapest(products)
                        binding.storeCountLabel.text =
                            "Available in ${products.size} stores"

                        // Savings banner
                        if (products.size > 1) {
                            val cheapest = products.first().price
                            val mostExpensive = products.last().price
                            val savings = mostExpensive - cheapest
                            binding.savingsText.text =
                                "You can save up to ${savings.toCurrencyString()} " +
                                        "by choosing the cheapest store"
                        } else {
                            binding.summaryBanner.gone()
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.root.showSnackbar(state.message)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}