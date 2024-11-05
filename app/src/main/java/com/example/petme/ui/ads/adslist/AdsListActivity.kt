package com.example.petme.ui.ads.adslist

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petme.R
import com.example.petme.databinding.ActivityAdsListBinding
import com.example.petme.models.ClassifiedAd
import com.example.petme.adapters.AdsSingleColAdapter
import com.example.petme.ui.home.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore

class AdsListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CATEGORY = "category"
    }

    private lateinit var binding: ActivityAdsListBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var category: String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adsAdapter: AdsSingleColAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get category passed through intent
        category = intent.getStringExtra(EXTRA_CATEGORY) ?: "All"

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        binding.recyclerViewAds.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAds.setHasFixedSize(true)

        // Set up sort dropdown (Spinner)
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val spinner: Spinner = binding.spinnerSort
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sortOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set up Filter Button
        binding.btnFilter.setOnClickListener {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
            // Implement filter functionality here
        }

        // Initialize adapter for the RecyclerView
        adsAdapter = AdsSingleColAdapter(mutableListOf(),showDeleteButton = false){}  // Pass an empty mutable list
        binding.recyclerViewAds.adapter = adsAdapter

        // Fetch Ads from Firestore
        fetchAdsFromFirestore()
    }

    private fun fetchAdsFromFirestore() {
        // Reference to Firestore collection
        val adsCollection = firestore.collection("ads")

        // Fetch ads from Firestore
        adsCollection
            .get()
            .addOnSuccessListener { result ->
                val adsList = mutableListOf<ClassifiedAd>()

                // Loop through Firestore documents
                for (document in result) {
                    val ad = document.toObject(ClassifiedAd::class.java)
                    adsList.add(ad)
                }
                Log.d("adslist",adsList.toString())
                // Filter ads based on the selected category
                val filteredAds = if (category.isEmpty() || category == "All") {
                    adsList // Show all ads if category is empty or "All"
                } else {
                    adsList.filter { it.category.equals(category, ignoreCase = true) }
                }

                // Update the RecyclerView with the fetched ads
                adsAdapter.updateAds(filteredAds)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching ads: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
