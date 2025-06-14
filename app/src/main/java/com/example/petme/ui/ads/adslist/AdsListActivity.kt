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
import com.example.petme.adapters.AdsAdapter
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
    private var selectedCategory: String = "Odaberi Kategoriju"
    private var selectedRegion: String = "Odaberi Županiju"
    private var selectedTypeOfAd: String = "Odaberi tip oglasa"
    private var selectedBloodType: String = "Odaberi tip"
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
        val userId  = intent.getStringExtra("userId")
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
        if (userId.toString().trim().isEmpty() || userId == null)
        {
            fetchAdsFromFirestore()
        }
        else
        {
            fetchAdsByUserId(userId)
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
            selectedCategory = categorySpinner.selectedItem?.toString() ?: "Odaberi Katergoriju"
            selectedRegion = regionSpinner.selectedItem?.toString() ?: "Odaberi Županiju"
            selectedTypeOfAd = typeOfAdSpinner.selectedItem?.toString() ?: "Odaberi tip oglasa"
            selectedBloodType = bloodTypeSpinner.selectedItem?.toString() ?: "Odaberi tip" // Optional

            minPriceInput = etMinPrice.text.toString().trim().toDoubleOrNull()
            maxPriceInput = etMaxPrice.text.toString().trim().toDoubleOrNull()
            minAgeInput = etMinAge.text.toString().trim().toIntOrNull()
            maxAgeInput = etMaxAge.text.toString().trim().toIntOrNull()

            applyFilters(
                category = selectedCategory ?: "Odaberi Kategoriju", // Default to "Select Category" if null
                region = selectedRegion ?: "Odaberi Županiju",       // Default to "Select Region" if null
                typeOfAd = selectedTypeOfAd ?: "Odaberi tip oglasa", // Default to "Select type of ad" if null
                bloodType = selectedBloodType ?: "Odaberi tip",    // Default to "Select blood" if null
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
            selectedCategory = "Odaberi Kategoriju"
            selectedRegion = "Odaberi Županiju"
            selectedTypeOfAd = "Odaberi tip oglasa"
            selectedBloodType = "Odaberi tip"
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
        minPrice: Double?,
        maxPrice: Double?,
        minAge: Int?,
        maxAge: Int?
    ) {
        val trimmedCategory = category.trim()
        val trimmedRegion = region.trim()
        val trimmedTypeOfAd = typeOfAd.trim()
        val trimmedBloodType = bloodType.trim()

        // Determine which filters are active (non-empty and not default)
        val isCategoryActive = trimmedCategory.isNotEmpty() && trimmedCategory != "Odaberi Kategoriju"
        val isRegionActive = trimmedRegion.isNotEmpty() && trimmedRegion != "Odaberi Županiju"
        val isTypeOfAdActive = trimmedTypeOfAd.isNotEmpty() && trimmedTypeOfAd != "Odaberi tip oglasa"
        val isBloodTypeActive = trimmedBloodType.isNotEmpty() && trimmedBloodType != "Odaberi tip"
        val isMinPriceActive = minPrice != null
        val isMaxPriceActive = maxPrice != null
        val isMinAgeActive = minAge != null
        val isMaxAgeActive = maxAge != null

        // If no filters are active at all, just return all ads
        if (!(isCategoryActive || isRegionActive || isTypeOfAdActive || isBloodTypeActive ||
                    isMinPriceActive || isMaxPriceActive || isMinAgeActive || isMaxAgeActive)) {
            // no filtering needed
            // use all ads
            // example: updateAdapter(adsList)
            return
        }

        val filteredAds = adsList.filter { ad ->

            val matchCategory = !isCategoryActive || ad.category.trim().equals(trimmedCategory, ignoreCase = true)
            val matchRegion = !isRegionActive || ad.region.trim().equals(trimmedRegion, ignoreCase = true)
            val matchTypeOfAd = !isTypeOfAdActive || ad.typeOfAd.trim().equals(trimmedTypeOfAd, ignoreCase = true)
            val matchBloodType = !isBloodTypeActive || ad.bloodType.trim().equals(trimmedBloodType, ignoreCase = true)
            val matchMinPrice = !isMinPriceActive || ad.price >= minPrice!!
            val matchMaxPrice = !isMaxPriceActive || ad.price <= maxPrice!!
            val matchMinAge = !isMinAgeActive || ad.age >= minAge!!
            val matchMaxAge = !isMaxAgeActive || ad.age <= maxAge!!

            // Collect only active filters into booleans
            val matches = mutableListOf<Boolean>()

            if (isCategoryActive) matches.add(matchCategory)
            if (isRegionActive) matches.add(matchRegion)
            if (isTypeOfAdActive) matches.add(matchTypeOfAd)
            if (isBloodTypeActive) matches.add(matchBloodType)
            if (isMinPriceActive) matches.add(matchMinPrice)
            if (isMaxPriceActive) matches.add(matchMaxPrice)
            if (isMinAgeActive) matches.add(matchMinAge)
            if (isMaxAgeActive) matches.add(matchMaxAge)

            // Return true if ad matches at least one active filter
            matches.any { it }
        }

        // Use filteredAds as needed
        // e.g. updateAdapter(filteredAds)



    Log.d("FILTER", "Filtered: $filteredAds")
        Log.d("FILTER", "Original: $adsList")

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

    fun fetchAdsByUserId(userId: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("ads")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val adList = mutableListOf<ClassifiedAd>()
                for (document in documents) {
                    val ad = document.toObject(ClassifiedAd::class.java)
                    adList.add(ad)
                }
                Log.d("uvatio",adList.toString())
                val list =  adList
                adsAdapter.updateAds(list)            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching ads: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
