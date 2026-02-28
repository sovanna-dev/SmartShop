package com.smartshop.app.ui.tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartshop.app.databinding.FragmentSpendingTrackerBinding
import com.smartshop.app.ui.orders.OrdersAdapter
import com.smartshop.app.data.model.Resource
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.toCurrencyString
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SpendingTrackerFragment : Fragment() {

    private var _binding: FragmentSpendingTrackerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SpendingTrackerViewModel by viewModels()
    private lateinit var categoryAdapter: CategorySpendingAdapter
    private lateinit var recentOrdersAdapter: OrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpendingTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupButtons()
        observeStats()
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategorySpendingAdapter()
        binding.categoryRecyclerView.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        recentOrdersAdapter = OrdersAdapter()
        binding.recentOrdersRecyclerView.apply {
            adapter = recentOrdersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stats.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        hideAllCards()
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        val stats = state.data

                        if (stats.totalOrders == 0) {
                            binding.emptyContainer.visible()
                            hideAllCards()
                            return@collectLatest
                        }

                        binding.emptyContainer.gone()
                        showAllCards()
                        bindStats(stats)
                    }
                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.root.showSnackbar(state.message)
                    }
                }
            }
        }
    }

    private fun bindStats(stats: SpendingStats) {
        // This month card
        binding.monthTotal.text = stats.totalThisMonth.toCurrencyString()
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(Date())
        binding.monthSubtext.text = "spent in $monthName"

        // Stats row
        binding.totalOrdersValue.text = stats.totalOrders.toString()
        binding.avgOrderValue.text = stats.averageOrderValue.toCurrencyString()

        // Category breakdown
        categoryAdapter.submitList(stats.categoryBreakdown)

        // Recent orders
        recentOrdersAdapter.submitList(stats.recentOrders)
    }

    private fun showAllCards() {
        binding.monthCard.visible()
        binding.statsRow.visible()
        binding.categoryCard.visible()
        binding.recentCard.visible()
    }

    private fun hideAllCards() {
        binding.monthCard.gone()
        binding.statsRow.gone()
        binding.categoryCard.gone()
        binding.recentCard.gone()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}