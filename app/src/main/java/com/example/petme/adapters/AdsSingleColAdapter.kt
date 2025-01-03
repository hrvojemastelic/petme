package com.example.petme.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petme.R
import com.example.petme.models.ClassifiedAd
import com.example.petme.ui.fullAd.FullAdActivity

class AdsSingleColAdapter(
    var adsList: MutableList<ClassifiedAd>,
    private val showDeleteButton: Boolean,
    private val onDeleteAd: (ClassifiedAd) -> Unit // Add this parameter
) : RecyclerView.Adapter<AdsSingleColAdapter.AdViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ad_single_col_layout, parent, false)
        return AdViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        val ad = adsList[position]
        holder.bind(ad)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, FullAdActivity::class.java)
            intent.putExtra("adId", ad.id)
            intent.putExtra("userId", ad.userId)// Pass the adId as a String
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = adsList.size

    // ViewHolder class to bind the views
    inner class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adTitle: TextView = itemView.findViewById(R.id.adTitle)
        private val adDescription: TextView = itemView.findViewById(R.id.adDescription)
        private val adPrice: TextView = itemView.findViewById(R.id.adPrice)
        private val adImage: ImageView = itemView.findViewById(R.id.adImage)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btnDeleteAd) // Find delete button


        fun bind(ad: ClassifiedAd) {
            adTitle.text = ad.title
            adDescription.text = ad.description
            adPrice.text = "€${ad.price}"

            // Load image if available
            if (ad.imageUrls.isNotEmpty()) {
                Glide.with(adImage.context)
                    .load(ad.imageUrls[0])
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(adImage)
            } else {
                adImage.setImageResource(R.drawable.placeholder_image)
            }

            deleteButton.visibility = if (showDeleteButton) View.VISIBLE else View.GONE
            // Set delete button action
            deleteButton.setOnClickListener {
                onDeleteAd(ad) // Call the onDeleteAd function when clicked
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
