package com.smartshop.app.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smartshop.app.data.model.OnboardingPage
import com.smartshop.app.databinding.ItemOnboardingImageBinding

class OnboardingImageAdapter(
    private val pages: List<OnboardingPage>
) : RecyclerView.Adapter<OnboardingImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(
        private val binding: ItemOnboardingImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(page: OnboardingPage) {
            binding.onboardingImage.setImageResource(page.imageRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemOnboardingImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) =
        holder.bind(pages[position])

    override fun getItemCount() = pages.size
}