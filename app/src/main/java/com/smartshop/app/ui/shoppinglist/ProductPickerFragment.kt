package com.smartshop.app.ui.shoppinglist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartshop.app.data.model.ShoppingListItem
import com.smartshop.app.databinding.FragmentProductPickerBinding
import com.smartshop.app.data.model.Resource
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductPickerFragment : Fragment() {

    private var _binding: FragmentProductPickerBinding? = null
    private val binding get() = _binding!!

    // Reuse HomeViewModel for products + ShoppingListItemViewModel for adding
    private val listViewModel: ShoppingListItemViewModel by viewModels()
    private val pickerViewModel: com.smartshop.app.ui.home.HomeViewModel by viewModels()

    private val args: ProductPickerFragmentArgs by navArgs()
    private lateinit var pickerAdapter: ProductPickerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listViewModel.loadItems(args.listId)
        setupRecyclerView()
        setupSearch()
        setupButtons()
        observeProducts()
        observeAddState()
    }

    private fun observeAddState() {
        viewLifecycleOwner.lifecycleScope.launch {
            listViewModel.addState.collectLatest { state ->
                when (state) {
                    is Resource.Success -> {
                        binding.root.showSnackbar("Added to list ✓")
                        listViewModel.resetAddState()
                    }
                    is Resource.Error -> {
                        binding.root.showSnackbar(state.message)
                        listViewModel.resetAddState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupRecyclerView() {
        pickerAdapter = ProductPickerAdapter { product ->
            val item = ShoppingListItem(
                productId = product.id,
                name = product.name,
                quantity = 1,
                price = product.price,
                imageUrl = product.imageUrl
            )
            listViewModel.addItem(item)
        }
        binding.productsRecyclerView.apply {
            adapter = pickerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener { text ->
            pickerViewModel.setSearchQuery(text.toString())
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            pickerViewModel.filteredProducts.collectLatest { products ->
                if (products.isEmpty()) {
                    binding.emptyText.visible()
                    binding.productsRecyclerView.gone()
                } else {
                    binding.emptyText.gone()
                    binding.productsRecyclerView.visible()
                    pickerAdapter.submitList(products)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}