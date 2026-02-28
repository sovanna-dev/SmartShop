package com.smartshop.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.firebase.auth.FirebaseAuth
import com.smartshop.app.R
import com.smartshop.app.databinding.FragmentProfileBinding
import com.smartshop.app.utils.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var dataStoreManager: DataStoreManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserInfo()
        setupButtons()
    }

    private fun setupUserInfo() {
        val user = auth.currentUser
        binding.userName.text = user?.displayName ?: "User Name"
        binding.userEmail.text = user?.email ?: "user@email.com"
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_orders)
        }

        binding.btnShoppingLists.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_shoppingLists)
        }

        binding.btnTracker.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_tracker)
        }

        binding.btnLogout.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    logout()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            auth.signOut()
            // Reset any local storage if needed, but primarily navigate back to login
            findNavController().navigate(R.id.loginFragment, null, navOptions {
                popUpTo(R.id.nav_graph) { inclusive = true }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}