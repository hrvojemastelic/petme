package com.example.petme.ui.ads.adslist

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdsListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CATEGORY = "category"
    }

    private lateinit var binding: ActivityAdsListBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var category: String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adsAdapter: AdsSingleColAdapter
    private var adsList = mutableListOf<ClassifiedAd>()

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

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sortedList = when (position) {
                    0 -> adsAdapter.adsList.sortedBy { it.price }  // "Price: Low to High"
                    1 -> adsAdapter.adsList.sortedByDescending { it.price } // "Price: High to Low"
                    2 -> adsAdapter.adsList.sortedByDescending { it.date.toDate() } // Newest First
                    3 -> adsAdapter.adsList.sortedBy { it.date.toDate() } // Oldest First
                    else -> adsAdapter.adsList
                }
                // Update the RecyclerView with the sorted list
                adsAdapter.updateAds(sortedList)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No action needed here
            }
        }
        // Set up Filter Button
        binding.btnFilter.setOnClickListener {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
            // Implement filter functionality here
        }

        // Initialize adapter for the RecyclerView
        adsAdapter = AdsSingleColAdapter(mutableListOf(), showDeleteButton = false) {}
        binding.recyclerViewAds.adapter = adsAdapter

        // Fetch Ads from Firestore
        fetchAdsFromFirestore()
    }

    private fun fetchAdsFromFirestore() {
        val adsCollection = firestore.collection("ads")

        adsCollection
            .get()
            .addOnSuccessListener { result ->
                adsList.clear()

                for (document in result) {
                    Log.d("rezultat",document.toString())

                    val ad = document.toObject(ClassifiedAd::class.java)
                    adsList.add(ad)
                }
                Log.d("adslist", adsList.toString())

                // Filter ads based on the selected category
                val filteredAds = if (category.isEmpty() || category == "All") {
                    adsList
                } else {
                    adsList.filter { it.category.equals(category, ignoreCase = true) }
                }

                // Update RecyclerView with filtered ads
                adsAdapter.updateAds(filteredAds)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching ads: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
