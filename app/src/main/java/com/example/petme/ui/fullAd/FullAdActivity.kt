package com.example.petme.ui.fullAd

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager
import com.example.petme.R
import com.example.petme.adapters.ImagePagerAdapter
import com.example.petme.databinding.ActivityFullAdBinding
import com.example.petme.models.ClassifiedAd
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class FullAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullAdBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        val adId = intent.getStringExtra("adId") // Get adId as String

        if (adId != null) {
            fetchAdDetails(adId) // Fetch and display ad details
        } else {
            Toast.makeText(this, "Ad not found", Toast.LENGTH_SHORT).show()
            finish() // Close activity if adId is missing
        }
    }

    private fun fetchAdDetails(adId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("ads").document(adId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val ad = document.toObject(ClassifiedAd::class.java)
                    Log.d("ad full", ad.toString())
                    ad?.let { displayAdDetails(it) }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load ad details", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun displayAdDetails(ad: ClassifiedAd) {
        findViewById<TextView>(R.id.priceTextView).text = "$${ad.price}"
        findViewById<TextView>(R.id.titleTextView).text = ad.title
        findViewById<TextView>(R.id.descriptionTextView).text = ad.description
        findViewById<TextView>(R.id.age).text = "Age: ${ad.age}"
        findViewById<TextView>(R.id.breedTextView).text = ad.breed
        //Convert date
        val date = ad.date.toDate()
        // Format Date
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        findViewById<TextView>(R.id.date).text = formattedDate

        findViewById<TextView>(R.id.phoneNumber).text = "${ad.phoneNumber}"

        findViewById<TextView>(R.id.address).text =ad.address
        findViewById<TextView>(R.id.region).text = ad.region
        findViewById<TextView>(R.id.category).text = ad.category



        // Setup ViewPager with image URLs
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        viewPager.adapter = ImagePagerAdapter(this, ad.imageUrls)
    }

    private fun setupImageSlider(imageUrls: List<String>) {
        // Setup image slider here, or use a library for viewing images fullscreen on click
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
