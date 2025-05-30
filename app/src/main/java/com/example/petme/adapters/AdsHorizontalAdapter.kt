package com.example.petme.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petme.R
import com.example.petme.databinding.ItemAdHorizontalBinding
import com.example.petme.models.ClassifiedAd
import com.example.petme.ui.fullAd.FullAdActivity

class AdsHorizontalAdapter(private var adsList: MutableList<ClassifiedAd>) : RecyclerView.Adapter<AdsHorizontalAdapter.AdViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val binding = ItemAdHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        val ad = adsList[position]
        holder.bind(ad)
        Log.d("uvatio", ad.toString())

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, FullAdActivity::class.java).apply {
                putExtra("adId", ad.id)
                putExtra("userId", ad.userId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = adsList.size

    inner class AdViewHolder(private val binding: ItemAdHorizontalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ad: ClassifiedAd) {
            binding.adTitle.text = ad.title
            binding.adPrice.text = "$${ad.price}"

            if (ad.imageUrls.isNotEmpty()) {
                Glide.with(binding.adImage.context)
                    .load(ad.imageUrls[0])
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(binding.adImage)
            } else {
                binding.adImage.setImageResource(R.drawable.placeholder_image)
            }
        }
    }

    fun updateAds(newAds: List<ClassifiedAd>) {
        adsList.clear()
        adsList.addAll(newAds)
        notifyDataSetChanged()
    }
}
