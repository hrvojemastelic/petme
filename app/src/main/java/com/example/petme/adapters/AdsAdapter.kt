package com.example.petme.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petme.R
import com.example.petme.databinding.ItemAdBinding
import com.example.petme.models.ClassifiedAd

class AdsAdapter(private var adsList: MutableList<ClassifiedAd>) : RecyclerView.Adapter<AdsAdapter.AdViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val binding = ItemAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        val ad = adsList[position]
        holder.bind(ad)
    }

    override fun getItemCount(): Int = adsList.size

    // ViewHolder class to bind the views
    inner class AdViewHolder(private val binding: ItemAdBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ad: ClassifiedAd) {
            binding.adTitle.text = ad.title
            binding.adDescription.text = ad.description
            binding.adPrice.text = "$${ad.price}"

            // Check if imageUrls is not empty and load the first image, else show placeholder
            if (ad.imageUrls.isNotEmpty()) {
                Glide.with(binding.adImage.context)
                    .load(ad.imageUrls[0]) // Load the first image URL
                    .placeholder(R.drawable.placeholder_image) // Placeholder while loading
                    .error(R.drawable.placeholder_image) // In case of error, show placeholder
                    .into(binding.adImage) // Set image to ImageView
            } else {
                // Set placeholder image if no image URLs are available
                binding.adImage.setImageResource(R.drawable.placeholder_image)
            }
        }
    }

    // Method to update ads dynamically
    fun updateAds(newAds: List<ClassifiedAd>) {
        adsList.clear()
        adsList.addAll(newAds)
        notifyDataSetChanged() // Notify the adapter that data has changed
    }
}
