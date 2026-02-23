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
        animateSplash()
    }

    private fun animateSplash() {
        // Logo pops in
        binding.splashLogo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(200)
            .start()

        // Title fades in
        binding.splashTitle.animate()
            .alpha(1f)
            .translationYBy(-10f)
            .setDuration(500)
            .setStartDelay(500)
            .start()

        // Tagline fades in
        binding.splashTagline.animate()
            .alpha(0.8f)
            .setDuration(400)
            .setStartDelay(700)
            .start()



        // Navigate after 2.5s
        lifecycleScope.launch {
            delay(2500)
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