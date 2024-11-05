package com.example.petme.ui.fullAd

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager
import com.example.petme.R
import com.example.petme.adapters.ImagePagerAdapter
import com.example.petme.databinding.ActivityFullAdBinding
import com.example.petme.models.ClassifiedAd
import com.google.firebase.firestore.FirebaseFirestore

class FullAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullAdBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullAdBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
                    ad?.let { displayAdDetails(it) }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load ad details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayAdDetails(ad: ClassifiedAd) {
        findViewById<TextView>(R.id.priceTextView).text = "$${ad.price}"
        findViewById<TextView>(R.id.titleTextView).text = ad.title
        findViewById<TextView>(R.id.descriptionTextView).text = ad.description
        findViewById<TextView>(R.id.ageTextView).text = "Age: ${ad.age} years"
        findViewById<TextView>(R.id.breedTextView).text = "Breed: ${ad.breed}"

        // Setup ViewPager with image URLs
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        viewPager.adapter = ImagePagerAdapter(this, ad.imageUrls)
    }

    private fun setupImageSlider(imageUrls: List<String>) {
        // Setup image slider here, or use a library for viewing images fullscreen on click
    }
}
