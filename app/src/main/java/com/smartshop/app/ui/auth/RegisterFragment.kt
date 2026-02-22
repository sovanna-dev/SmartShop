package com.smartshop.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.smartshop.app.R
import com.smartshop.app.data.model.Resource
import com.smartshop.app.databinding.FragmentRegisterBinding
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.isValidEmail
import com.smartshop.app.utils.isValidPassword
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeRegisterState()
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            validateAndRegister()
        }

        binding.loginLink.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun validateAndRegister() {
        val name = binding.nameInput.text?.toString()?.trim() ?: ""
        val email = binding.emailInput.text?.toString()?.trim() ?: ""
        val password = binding.passwordInput.text?.toString() ?: ""
        val confirmPassword = binding.confirmPasswordInput.text?.toString() ?: ""

        // Clear previous errors
        binding.nameLayout.error = null
        binding.emailLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmPasswordLayout.error = null

        var isValid = true

        if (name.isEmpty()) {
            binding.nameLayout.error = "Name is required"
            isValid = false
        }

        if (!email.isValidEmail()) {
            binding.emailLayout.error = "Enter a valid email address"
            isValid = false
        }

        if (!password.isValidPassword()) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        }

        if (isValid) viewModel.register(name, email, password)
    }

    private fun observeRegisterState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registerState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> showLoading(true)
                    is Resource.Success -> {
                        showLoading(false)
                        viewModel.resetRegisterState()
                        findNavController().navigate(R.id.action_register_to_home)
                    }
                    is Resource.Error -> {
                        showLoading(false)
                        binding.root.showSnackbar(state.message)
                    }
                    null -> Unit
                }
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        if (loading) {
            binding.progressBar.visible()
            binding.registerButton.isEnabled = false
        } else {
            binding.progressBar.gone()
            binding.registerButton.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}