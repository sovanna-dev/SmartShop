package com.smartshop.app.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.smartshop.app.R
import com.smartshop.app.databinding.FragmentSplashBinding
import com.smartshop.app.utils.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var dataStoreManager: DataStoreManager
    @Inject lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animate logo on splash
        binding.splashLogo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .start()

        binding.splashTitle.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(300)
            .start()

        lifecycleScope.launch {
            // Show splash for 2 seconds
            delay(2000)

            val isFirstLaunch = dataStoreManager.isFirstLaunch.first()
            val isLoggedIn = firebaseAuth.currentUser != null

            val destination = when {
                isFirstLaunch -> R.id.action_splash_to_onboarding
                isLoggedIn    -> R.id.action_splash_to_home
                else          -> R.id.action_splash_to_login
            }

            findNavController().navigate(destination)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}