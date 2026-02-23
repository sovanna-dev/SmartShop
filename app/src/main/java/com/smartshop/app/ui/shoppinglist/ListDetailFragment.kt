package com.smartshop.app.ui.shoppinglist

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smartshop.app.data.model.ShoppingListItem
import com.smartshop.app.databinding.FragmentListDetailBinding
import com.smartshop.app.utils.Resource
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListDetailFragment : Fragment() {

    private var _binding: FragmentListDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ListDetailViewModel by viewModels()
    private val args: ListDetailFragmentArgs by navArgs()
    private lateinit var itemAdapter: ListItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listTitle.text = args.listName
        viewModel.loadItems(args.listId)
        setupRecyclerView()
        setupButtons()
        observeItems()
    }

    private fun setupRecyclerView() {
        itemAdapter = ListItemAdapter(
            onCheckedChange = { item, isChecked ->
                viewModel.toggleChecked(item.id, isChecked)
            },
            onRemoveClick = { item ->
                viewModel.removeItem(item.id)
            }
        )
        binding.itemsRecyclerView.apply {
            adapter = itemAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.addItemFab.setOnClickListener {
            showAddItemDialog()
        }
    }

    private fun showAddItemDialog() {
        // Navigate to product picker — user picks from existing products
        findNavController().navigate(
            ListDetailFragmentDirections.actionListDetailToProductPicker(args.listId)
        )
    }

    private fun observeItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.items.collectLatest { state ->
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
                            binding.itemsRecyclerView.gone()
                        } else {
                            binding.emptyContainer.gone()
                            binding.itemsRecyclerView.visible()
                            itemAdapter.submitList(items)
                            val checked = items.count { it.isChecked }
                            binding.progressSummary.text = "$checked/${items.size}"
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