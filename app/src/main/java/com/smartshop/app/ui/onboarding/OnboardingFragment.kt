package com.smartshop.app.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.smartshop.app.R
import com.smartshop.app.data.model.OnboardingPage
import com.smartshop.app.databinding.FragmentOnboardingBinding
import com.smartshop.app.utils.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    // Hilt injects this automatically
    @Inject
    lateinit var dataStoreManager: DataStoreManager

    // Define the 3 onboarding pages
    private val pages = listOf(
        OnboardingPage(
            imageRes = R.mipmap.ic_launcher,
            title = "Discover Products",
            description = "Browse thousands of products across all categories at the best prices."
        ),
        OnboardingPage(
            imageRes = R.mipmap.ic_launcher,
            title = "Easy Shopping",
            description = "Add items to your cart and manage your orders with just a few taps."
        ),
        OnboardingPage(
            imageRes = R.mipmap.ic_launcher,
            title = "Fast Delivery",
            description = "Get your orders delivered quickly right to your doorstep."
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupIndicatorDots()
        setupButtons()
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(pages)
        binding.viewPager.adapter = adapter

        // Listen for page changes
        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateIndicatorDots(position)
                    updateButtons(position)
                }
            }
        )
    }

    private fun setupIndicatorDots() {
        // Create one dot per page
        pages.forEachIndexed { index, _ ->
            val dot = ImageView(requireContext())
            dot.setImageResource(
                if (index == 0) R.drawable.dot_active
                else R.drawable.dot_inactive
            )
            val params = ViewGroup.MarginLayoutParams(24, 24)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            binding.indicatorDots.addView(dot)
        }
    }

    private fun updateIndicatorDots(position: Int) {
        for (i in 0 until binding.indicatorDots.childCount) {
            val dot = binding.indicatorDots.getChildAt(i) as ImageView
            dot.setImageResource(
                if (i == position) R.drawable.dot_active
                else R.drawable.dot_inactive
            )
        }
    }

    private fun setupButtons() {
        binding.nextButton.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.size - 1) {
                binding.viewPager.currentItem = current + 1
            }
        }

        binding.skipButton.setOnClickListener {
            navigateToLogin()
        }

        binding.getStartedButton.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun updateButtons(position: Int) {
        val isLastPage = position == pages.size - 1

        // Last page: hide Next/Skip, show Get Started
        binding.nextButton.visibility = if (isLastPage) View.GONE else View.VISIBLE
        binding.skipButton.visibility = if (isLastPage) View.GONE else View.VISIBLE
        binding.getStartedButton.visibility = if (isLastPage) View.VISIBLE else View.GONE
    }

    private fun navigateToLogin() {
        // Save that onboarding is done — never show again
        lifecycleScope.launch {
            dataStoreManager.setFirstLaunchDone()
            findNavController().navigate(
                R.id.action_onboarding_to_login
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}