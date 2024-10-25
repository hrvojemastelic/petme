// ImageSliderAdapter.kt
package com.example.njuapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.example.petme.R

class ImageSliderAdapter(private val context: Context, private val images: List<Int>) :
    RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder>() {

    // ViewHolder to hold the ImageView
    inner class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewSlider) // Make sure this ID matches your layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        // Inflate the layout for each item
        val view = LayoutInflater.from(context).inflate(R.layout.item_slider_image, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        // Set the image resource for the current position
        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount(): Int = images.size
}