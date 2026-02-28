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
import com.smartshop.app.data.model.Resource
import com.smartshop.app.databinding.FragmentListDetailBinding
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

    private val viewModel: ShoppingListItemViewModel by viewModels()
    private val args: ListDetailFragmentArgs by navArgs()
    private lateinit var itemsAdapter: ShoppingListItemAdapter

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

        setupRecyclerView()
        setupButtons()
        observeItems()
        observeActionState()

        viewModel.loadItems(args.listId)
    }

    private fun observeActionState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.actionState.collectLatest { state ->
                when (state) {
                    is Resource.Error -> {
                        binding.root.showSnackbar(state.message)
                        viewModel.resetActionState()
                    }
                    is Resource.Success -> {
                        // Optionally show "Item removed" snackbar
                        viewModel.resetActionState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupRecyclerView() {
        itemsAdapter = ShoppingListItemAdapter(
            onCheckedChange = { item, isChecked ->
                viewModel.toggleItem(item.id, isChecked)
            },
            onDeleteClick = { item ->
                viewModel.removeItem(item.id, item.isChecked)
            }
        )
        binding.itemsRecyclerView.apply {
            adapter = itemsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.addItemFab.setOnClickListener {
            val action = ListDetailFragmentDirections
                .actionListDetailToProductPicker(args.listId)
            findNavController().navigate(action)
        }
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
                        val checked = items.count { it.isChecked }
                        val total = items.size

                        // Update progress summary in header e.g. "1/3"
                        binding.progressSummary.text = "$checked/$total"

                        if (items.isEmpty()) {
                            binding.emptyContainer.visible()
                            binding.itemsRecyclerView.gone()
                        } else {
                            binding.emptyContainer.gone()
                            binding.itemsRecyclerView.visible()
                            // submitSorted puts unchecked first, checked at bottom
                            itemsAdapter.submitSorted(items)
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