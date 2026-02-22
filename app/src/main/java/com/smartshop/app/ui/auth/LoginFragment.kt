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
import com.smartshop.app.databinding.FragmentLoginBinding
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.isValidEmail
import com.smartshop.app.utils.isValidPassword
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // viewModels() — Hilt creates and manages this automatically
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeLoginState()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            validateAndLogin()
        }

        binding.registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.forgotPasswordLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotPassword)
        }
    }

    private fun validateAndLogin() {
        val email = binding.emailInput.text?.toString()?.trim() ?: ""
        val password = binding.passwordInput.text?.toString() ?: ""

        // Clear previous errors
        binding.emailLayout.error = null
        binding.passwordLayout.error = null

        // Validate inputs
        var isValid = true

        if (!email.isValidEmail()) {
            binding.emailLayout.error = "Enter a valid email address"
            isValid = false
        }

        if (!password.isValidPassword()) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (isValid) viewModel.login(email, password)
    }

    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        showLoading(true)
                    }
                    is Resource.Success -> {
                        showLoading(false)
                        viewModel.resetLoginState()
                        findNavController().navigate(R.id.action_login_to_home)
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
            binding.loginButton.isEnabled = false
        } else {
            binding.progressBar.gone()
            binding.loginButton.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}