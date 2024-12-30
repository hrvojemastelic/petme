package com.example.petme.ui.ads.adslist

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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

    // Variables to store filter selections
    private var selectedCategory: String = "Select Category"
    private var selectedRegion: String = "Select Region"
    private var selectedTypeOfAd: String = "Select type of ad"
    private var selectedBloodType: String = "Select blood"
    private var minPriceInput: Double? = null
    private var maxPriceInput: Double? = null
    private var minAgeInput: Int? = null
    private var maxAgeInput: Int? = null

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

        setupActionBar()
        // Get category passed through intent
        category = intent.getStringExtra(EXTRA_CATEGORY) ?: "All"

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        binding.recyclerViewAds.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAds.setHasFixedSize(true)
        binding.recyclerViewAds.clipToPadding = false

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
            showFilterDialog()
            // Implement filter functionality here
        }

        // Initialize adapter for the RecyclerView
        adsAdapter = AdsSingleColAdapter(mutableListOf(), showDeleteButton = false) {}
        binding.recyclerViewAds.adapter = adsAdapter

        // Fetch Ads from Firestore
        fetchAdsFromFirestore()
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
                val filteredAds = if (category.isEmpty() || category == "") {
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


    private fun showFilterDialog() {

        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)

        // Initialize UI elements
        val categorySpinner: Spinner = dialogView.findViewById(R.id.spinnerCategory)
        val regionSpinner: Spinner = dialogView.findViewById(R.id.spinnerRegion)
        val typeOfAdSpinner: Spinner = dialogView.findViewById(R.id.spinnerTypeOfAd)
        val bloodTypeSpinner: Spinner = dialogView.findViewById(R.id.bloodTypeSpinner) // Optional
        val etMinPrice: EditText = dialogView.findViewById(R.id.etMinPrice)
        val etMaxPrice: EditText = dialogView.findViewById(R.id.etMaxPrice)
        val etMinAge: EditText = dialogView.findViewById(R.id.etMinAge)
        val etMaxAge: EditText = dialogView.findViewById(R.id.etMaxAge)


        // Restore previous filter values
        categorySpinner.setSelection((resources.getStringArray(R.array.category_options).indexOf(selectedCategory)))
        regionSpinner.setSelection((resources.getStringArray(R.array.croatian_regions).indexOf(selectedRegion)))
        typeOfAdSpinner.setSelection((resources.getStringArray(R.array.typeOfAd).indexOf(selectedTypeOfAd)))
        bloodTypeSpinner.setSelection((resources.getStringArray(R.array.bloodType).indexOf(selectedBloodType)))
        etMinPrice.setText(minPriceInput?.toString())
        etMaxPrice.setText(maxPriceInput?.toString())
        etMinAge.setText(minAgeInput?.toString())
        etMaxAge.setText(maxAgeInput?.toString())

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Filter Ads")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Cancel button to dismiss dialog
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        // Apply filters and hide dialog
        dialogView.findViewById<Button>(R.id.btnApply).setOnClickListener {
            selectedCategory = categorySpinner.selectedItem?.toString() ?: "Select Category"
            selectedRegion = regionSpinner.selectedItem?.toString() ?: "Select Region"
            selectedTypeOfAd = typeOfAdSpinner.selectedItem?.toString() ?: "Select type of ad"
            selectedBloodType = bloodTypeSpinner.selectedItem?.toString() ?: "Select blood" // Optional

            minPriceInput = etMinPrice.text.toString().trim().toDoubleOrNull()
            maxPriceInput = etMaxPrice.text.toString().trim().toDoubleOrNull()
            minAgeInput = etMinAge.text.toString().trim().toIntOrNull()
            maxAgeInput = etMaxAge.text.toString().trim().toIntOrNull()

            applyFilters(
                category = selectedCategory ?: "Select Category", // Default to "Select Category" if null
                region = selectedRegion ?: "Select Region",       // Default to "Select Region" if null
                typeOfAd = selectedTypeOfAd ?: "Select type of ad", // Default to "Select type of ad" if null
                bloodType = selectedBloodType ?: "Select blood",    // Default to "Select blood" if null
                minPrice = minPriceInput,
                maxPrice = maxPriceInput,
                minAge = minAgeInput,
                maxAge = maxAgeInput
            )

            Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // Remove all filters and reset the list
        dialogView.findViewById<Button>(R.id.btnRemoveFilters).setOnClickListener {
            selectedCategory = "Select Category"
            selectedRegion = "Select Region"
            selectedTypeOfAd = "Select type of ad"
            selectedBloodType = "Select blood"
            minPriceInput = null
            maxPriceInput = null
            minAgeInput = null
            maxAgeInput = null

            resetFilters()

            Toast.makeText(this, "Filters removed", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun applyFilters(
        category: String,
        region: String,
        typeOfAd: String,
        bloodType: String,
        minPrice: Double?, // These remain as Double for price range
        maxPrice: Double?,
        minAge: Int?,      // These remain as Int for age range
        maxAge: Int?
    ) {
        val filteredAds = adsList.filter { ad ->
            val matchesCategory = category == "Select Category" || ad.category == category
            val matchesRegion = region == "Select Region" || ad.region == region
            val matchesTypeOfAd = typeOfAd == "Select type of ad" || ad.typeOfAd == typeOfAd
            val matchesBloodType = bloodType == "Select blood" || ad.bloodType == bloodType
            val matchesMinPrice = minPrice == null || ad.price >= minPrice
            val matchesMaxPrice = maxPrice == null || ad.price <= maxPrice
            val matchesMinAge = minAge == null || ad.age >= minAge
            val matchesMaxAge = maxAge == null || ad.age <= maxAge

            // Combine all the conditions
            matchesCategory && matchesRegion && matchesTypeOfAd &&
                    matchesBloodType  &&
                    matchesMinPrice && matchesMaxPrice &&
                    matchesMinAge && matchesMaxAge
        }

        adsAdapter.updateAds(filteredAds)

        if (filteredAds.isEmpty()) {
            Toast.makeText(this, "No ads match the selected filters", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetFilters() {
        // Clear any applied filters and reload the unfiltered data
        fetchAdsFromFirestore()
    // Replace this with your actual method to fetch and display the unfiltered list
    }

    private fun searchForAds(query: String) {
        // Implement search logic here
    }
}
