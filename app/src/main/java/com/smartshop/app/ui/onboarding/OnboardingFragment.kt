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

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    private val pages = listOf(
        OnboardingPage(
            imageRes = R.mipmap.ic_launcher,
            title = "Discover Amazing",
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

    // Split titles into two lines for design effect
    private val titleHighlights = listOf("Products", "Experience", "Guaranteed")

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
        updateContent(0)
    }

    private fun setupViewPager() {
        val adapter = OnboardingImageAdapter(pages)
        binding.viewPagerImage.adapter = adapter

        binding.viewPagerImage.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateIndicatorDots(position)
                    updateButtons(position)
                    updateContent(position)
                }
            }
        )
    }

    private fun updateContent(position: Int) {
        binding.pageTitle.text = pages[position].title
        binding.pageTitleHighlight.text = titleHighlights[position]
        binding.pageDescription.text = pages[position].description
    }

    private fun setupIndicatorDots() {
        pages.forEachIndexed { index, _ ->
            val dot = ImageView(requireContext())
            dot.setImageResource(
                if (index == 0) R.drawable.dot_active_orange
                else R.drawable.dot_inactive_orange
            )
            val params = android.widget.LinearLayout.LayoutParams(
                if (index == 0) 24 else 20,
                if (index == 0) 24 else 20
            )
            params.setMargins(0, 0, 12, 0)
            dot.layoutParams = params
            binding.indicatorDots.addView(dot)
        }
    }

    private fun updateIndicatorDots(position: Int) {
        for (i in 0 until binding.indicatorDots.childCount) {
            val dot = binding.indicatorDots.getChildAt(i) as ImageView
            val isActive = i == position
            dot.setImageResource(
                if (isActive) R.drawable.dot_active_orange
                else R.drawable.dot_inactive_orange
            )
            val size = if (isActive) 24 else 20
            val params = android.widget.LinearLayout.LayoutParams(size, size)
            params.setMargins(0, 0, 12, 0)
            dot.layoutParams = params
        }
    }
    private fun setupButtons() {
        binding.nextButton.setOnClickListener {
            val current = binding.viewPagerImage.currentItem
            if (current < pages.size - 1) {
                binding.viewPagerImage.currentItem = current + 1
            }
        }

        binding.skipButton.setOnClickListener { navigateToLogin() }
        binding.getStartedButton.setOnClickListener { navigateToLogin() }
    }

    private fun updateButtons(position: Int) {
        val isLastPage = position == pages.size - 1
        binding.nextButton.visibility = if (isLastPage) View.GONE else View.VISIBLE
        binding.skipButton.visibility = if (isLastPage) View.GONE else View.VISIBLE
        binding.getStartedButton.visibility = if (isLastPage) View.VISIBLE else View.GONE
    }

    private fun navigateToLogin() {
        lifecycleScope.launch {
            dataStoreManager.setFirstLaunchDone()
            findNavController().navigate(R.id.action_onboarding_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}