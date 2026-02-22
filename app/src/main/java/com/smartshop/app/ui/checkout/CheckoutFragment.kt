package com.smartshop.app.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.smartshop.app.R
import com.smartshop.app.data.model.Address
import com.smartshop.app.data.model.CartItem
import com.smartshop.app.data.model.Resource
import com.smartshop.app.databinding.FragmentCheckoutBinding
import com.smartshop.app.ui.cart.CartViewModel
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.toCurrencyString
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private val checkoutViewModel: CheckoutViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeCart()
        observeOrderState()

        binding.placeOrderButton.setOnClickListener {
            validateAndPlaceOrder()
        }
    }

    private fun observeCart() {
        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.cartItems.collectLatest { state ->
                if (state is Resource.Success) {
                    val items = state.data
                    val totalItems = items.sumOf { it.quantity }
                    binding.itemCountValue.text = "$totalItems items"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.cartTotal.collectLatest { total ->
                binding.totalAmountText.text = total.toCurrencyString()
            }
        }
    }

    private fun validateAndPlaceOrder() {
        val street = binding.streetInput.text?.toString()?.trim() ?: ""
        val city = binding.cityInput.text?.toString()?.trim() ?: ""
        val state = binding.stateInput.text?.toString()?.trim() ?: ""
        val zip = binding.zipInput.text?.toString()?.trim() ?: ""
        val country = binding.countryInput.text?.toString()?.trim() ?: ""

        // Clear errors
        binding.streetLayout.error = null
        binding.cityLayout.error = null
        binding.stateLayout.error = null
        binding.zipLayout.error = null
        binding.countryLayout.error = null

        // Validate
        var isValid = true

        if (street.isEmpty()) {
            binding.streetLayout.error = "Street address is required"
            isValid = false
        }
        if (city.isEmpty()) {
            binding.cityLayout.error = "City is required"
            isValid = false
        }
        if (state.isEmpty()) {
            binding.stateLayout.error = "State is required"
            isValid = false
        }
        if (zip.isEmpty()) {
            binding.zipLayout.error = "Zip code is required"
            isValid = false
        }
        if (country.isEmpty()) {
            binding.countryLayout.error = "Country is required"
            isValid = false
        }

        if (!isValid) return

        val address = Address(
            street = street,
            city = city,
            state = state,
            zipCode = zip,
            country = country
        )

        val cartState = cartViewModel.cartItems.value
        if (cartState !is Resource.Success) {
            binding.root.showSnackbar("Cart is empty")
            return
        }

        checkoutViewModel.placeOrder(
            items = cartState.data,
            totalPrice = cartViewModel.cartTotal.value,
            shippingAddress = address
        )
    }

    private fun observeOrderState() {
        viewLifecycleOwner.lifecycleScope.launch {
            checkoutViewModel.orderState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.placeOrderButton.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        binding.root.showSnackbar("Order placed successfully!")
                        checkoutViewModel.resetOrderState()
                        // Navigate to orders screen
                        findNavController().navigate(
                            R.id.action_checkout_to_orders
                        )
                    }
                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.placeOrderButton.isEnabled = true
                        binding.root.showSnackbar(state.message)
                    }
                    null -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}