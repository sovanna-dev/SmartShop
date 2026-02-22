package com.smartshop.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.smartshop.app.data.model.Resource
import com.smartshop.app.databinding.FragmentForgotPasswordBinding
import com.smartshop.app.utils.gone
import com.smartshop.app.utils.isValidEmail
import com.smartshop.app.utils.showSnackbar
import com.smartshop.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendResetButton.setOnClickListener {
            val email = binding.emailInput.text?.toString()?.trim() ?: ""
            if (!email.isValidEmail()) {
                binding.emailLayout.error = "Enter a valid email address"
                return@setOnClickListener
            }
            binding.emailLayout.error = null
            viewModel.forgotPassword(email)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.forgotPasswordState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.sendResetButton.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        binding.root.showSnackbar("Reset link sent. Check your email.")
                        findNavController().popBackStack()
                    }
                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.sendResetButton.isEnabled = true
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