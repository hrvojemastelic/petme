package com.example.petme.ui.fullAd.fullimage
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.viewpager2.widget.ViewPager2
import com.example.petme.R
import com.example.petme.adapters.ImageViewPagerAdapter

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var imageUrls: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

      setupActionBar()
        // Retrieve the image URLs from the intent
        imageUrls = intent.getStringArrayListExtra(EXTRA_IMAGE_URLS) ?: emptyList()

        viewPager = findViewById(R.id.viewPager)
        val adapter = ImageViewPagerAdapter(this, imageUrls)
        viewPager.adapter = adapter

        // Set the initial image position if passed
        val initialPosition = intent.getIntExtra(EXTRA_IMAGE_POSITION, 0)
        viewPager.setCurrentItem(initialPosition, false)
    }

    companion object {
        private const val EXTRA_IMAGE_URLS = "extra_image_urls"
        private const val EXTRA_IMAGE_POSITION = "extra_image_position"

        fun newIntent(context: Context, imageUrls: List<String>, initialPosition: Int): Intent {
            return Intent(context, FullScreenImageActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_IMAGE_URLS, ArrayList(imageUrls))
                putExtra(EXTRA_IMAGE_POSITION, initialPosition)
            }
        }
    }

    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)

        val customView = layoutInflater.inflate(R.layout.custom_action_bar, null)
        actionBar?.customView = customView

        val searchView = customView.findViewById<SearchView>(R.id.searchView)
        searchView.setIconifiedByDefault(false)
        searchView.clearFocus()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchForAds(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?) = true
        })
    }
    private fun searchForAds(query: String) {
        // Implement search logic here
    }
}
