package com.example.petme.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.example.petme.R
import com.example.petme.ui.fullAd.fullimage.FullScreenImageActivity

class ImagePagerAdapter(
    private val context: Context,
    private val imageUrls: List<String>
) : PagerAdapter() {

    override fun getCount(): Int = imageUrls.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image_pager, container, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView)

        // Load image using Glide
        Glide.with(context)
            .load(imageUrls[position])
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(imageView)

        imageView.setOnClickListener {
            // Open FullScreenImageActivity when image is clicked
            val intent = FullScreenImageActivity.newIntent(context, imageUrls, position)
            context.startActivity(intent)
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
