package com.smartshop.app.ui.shoppinglist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.smartshop.app.R
import com.smartshop.app.databinding.FragmentShoppingListsBinding
import com.smartshop.app.utils.Resource
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShoppingListsFragment : Fragment() {

    private var _binding: FragmentShoppingListsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ShoppingListViewModel by viewModels()
    private lateinit var listAdapter: ShoppingListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        observeLists()
        observeCreateState()
    }

    private fun setupRecyclerView() {
        listAdapter = ShoppingListAdapter(
            onListClick = { list ->
                val action = ShoppingListsFragmentDirections
                    .actionShoppingListsToListDetail(list.id, list.name)
                findNavController().navigate(action)
            },
            onDeleteClick = { list ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete List")
                    .setMessage("Delete \"${list.name}\"? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteList(list.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.listsRecyclerView.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.createListFab.setOnClickListener {
            showCreateListDialog()
        }
    }

    private fun showCreateListDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_list, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.listNameInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Shopping List")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.createList(name)
                } else {
                    binding.root.showSnackbar("Please enter a list name")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeLists() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lists.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.emptyContainer.gone()
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        if (state.data.isEmpty()) {
                            binding.emptyContainer.visible()
                            binding.listsRecyclerView.gone()
                        } else {
                            binding.emptyContainer.gone()
                            binding.listsRecyclerView.visible()
                            listAdapter.submitList(state.data)
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

    private fun observeCreateState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createState.collectLatest { state ->
                when (state) {
                    is Resource.Success -> {
                        binding.root.showSnackbar("List created!")
                        viewModel.resetCreateState()
                    }
                    is Resource.Error -> {
                        binding.root.showSnackbar(state.message)
                        viewModel.resetCreateState()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}