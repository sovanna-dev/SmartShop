package com.smartshop.app.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartshop.app.R
import com.smartshop.app.data.model.Order
import com.smartshop.app.data.model.OrderStatus
import com.smartshop.app.databinding.FragmentOrdersBinding
import com.smartshop.app.data.model.Resource
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrdersViewModel by viewModels()
    private lateinit var ordersAdapter: OrdersAdapter
    private var selectedTab = "ALL"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupToolbar()
        setupTabs()
        observeOrders()
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter()
        binding.ordersRecyclerView.apply {
            adapter = ordersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.trackerButton.setOnClickListener {
            findNavController().navigate(R.id.action_orders_to_tracker)
        }
    }

    private fun setupTabs() {
        binding.tabAll.setOnClickListener { selectTab("ALL") }
        binding.tabActive.setOnClickListener { selectTab("ACTIVE") }
        binding.tabCompleted.setOnClickListener { selectTab("COMPLETED") }
        binding.tabCancelled.setOnClickListener { selectTab("CANCELLED") }
    }

    private fun selectTab(tab: String) {
        selectedTab = tab

        // Update tab styles
        listOf(
            binding.tabAll to "ALL",
            binding.tabActive to "ACTIVE",
            binding.tabCompleted to "COMPLETED",
            binding.tabCancelled to "CANCELLED"
        ).forEach { (view, tabName) ->
            val isSelected = tabName == tab
            view.setBackgroundResource(
                if (isSelected) R.drawable.bg_tab_active
                else R.drawable.bg_tab_inactive
            )
            view.setTextColor(
                if (isSelected) android.graphics.Color.WHITE
                else android.graphics.Color.parseColor("#6B3A2A")
            )
        }

        // Always use latest data from ViewModel
        val currentData = (viewModel.orders.value as? Resource.Success)?.data ?: emptyList()
        filterOrders(currentData)
    }

    private fun filterOrders(allOrders: List<Order> = emptyList()) {
        val filtered = when (selectedTab) {
            "ACTIVE" -> allOrders.filter {
                it.status == OrderStatus.PENDING ||
                        it.status == OrderStatus.CONFIRMED ||
                        it.status == OrderStatus.SHIPPED
            }
            "COMPLETED" -> allOrders.filter {
                it.status == OrderStatus.DELIVERED
            }
            "CANCELLED" -> allOrders.filter {
                it.status == OrderStatus.CANCELLED
            }
            else -> allOrders
        }

        if (filtered.isEmpty()) {
            binding.emptyContainer.visible()
            binding.ordersRecyclerView.gone()
        } else {
            binding.emptyContainer.gone()
            binding.ordersRecyclerView.visible()
            ordersAdapter.submitList(filtered)
        }
    }

    private fun observeOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.orders.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.emptyContainer.gone()
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        filterOrders(state.data)
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