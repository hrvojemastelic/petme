package com.example.petme.ui.ads.adslist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petme.R
import com.example.petme.adapters.AdsSingleColAdapter
import com.example.petme.databinding.ActivityAdsListBinding
import com.example.petme.models.ClassifiedAd
import com.example.petme.ui.home.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore

class AdsListFragment : Fragment() {

    companion object {
        private const val ARG_CATEGORY = "category"
        private const val ARG_USER_ID = "userId"

        fun newInstance(category: String?, userId: String?): AdsListFragment {
            val fragment = AdsListFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: ActivityAdsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adsAdapter: AdsSingleColAdapter
    private var adsList = mutableListOf<ClassifiedAd>()

    // Filters
    private var selectedCategory: String = "Odaberi Kategoriju"
    private var selectedRegion: String = "Odaberi Županiju"
    private var selectedTypeOfAd: String = "Odaberi Tip Oglasa"
    private var selectedBloodType: String = "Odaberi Tip"
    private var minPriceInput: Double? = null
    private var maxPriceInput: Double? = null
    private var minAgeInput: Int? = null
    private var maxAgeInput: Int? = null

    private var category: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = AdsListFragmentArgs.fromBundle(requireArguments())
        category = args.category
        userId = args.userId
        firestore = FirebaseFirestore.getInstance()
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityAdsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupSortSpinner()

        binding.btnFilter.setOnClickListener { showFilterDialog() }

        adsAdapter = AdsSingleColAdapter(mutableListOf(), showDeleteButton = false) {}
        binding.recyclerViewAds.adapter = adsAdapter

        if (userId.isNullOrEmpty()) fetchAdsFromFirestore() else fetchAdsByUserId(userId!!)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewAds.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAds.setHasFixedSize(true)
        binding.recyclerViewAds.clipToPadding = false
    }

    private fun setupSortSpinner() {
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = adapter

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sortedList = when (position) {
                    0 -> adsAdapter.adsList.sortedBy { it.price }
                    1 -> adsAdapter.adsList.sortedByDescending { it.price }
                    2 -> adsAdapter.adsList.sortedByDescending { it.date.toDate() }
                    3 -> adsAdapter.adsList.sortedBy { it.date.toDate() }
                    else -> adsAdapter.adsList
                }
                adsAdapter.updateAds(sortedList)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchAdsFromFirestore() {
        firestore.collection("ads")
            .get()
            .addOnSuccessListener { result ->
                adsList.clear()
                for (doc in result) adsList.add(doc.toObject(ClassifiedAd::class.java))
                val filtered = if (category.isNullOrEmpty()) adsList else adsList.filter {
                    it.category.equals(category, ignoreCase = true)
                }
                adsAdapter.updateAds(filtered)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAdsByUserId(userId: String) {
        firestore.collection("ads")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                adsList.clear()
                for (doc in result) adsList.add(doc.toObject(ClassifiedAd::class.java))
                adsAdapter.updateAds(adsList)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)

        val categorySpinner: Spinner = dialogView.findViewById(R.id.spinnerCategory)
        val regionSpinner: Spinner = dialogView.findViewById(R.id.spinnerRegion)
        val typeOfAdSpinner: Spinner = dialogView.findViewById(R.id.spinnerTypeOfAd)
        val bloodTypeSpinner: Spinner = dialogView.findViewById(R.id.bloodTypeSpinner)
        val etMinPrice: EditText = dialogView.findViewById(R.id.etMinPrice)
        val etMaxPrice: EditText = dialogView.findViewById(R.id.etMaxPrice)
        val etMinAge: EditText = dialogView.findViewById(R.id.etMinAge)
        val etMaxAge: EditText = dialogView.findViewById(R.id.etMaxAge)

        categorySpinner.setSelection(resources.getStringArray(R.array.category_options).indexOf(selectedCategory))
        regionSpinner.setSelection(resources.getStringArray(R.array.croatian_regions).indexOf(selectedRegion))
        typeOfAdSpinner.setSelection(resources.getStringArray(R.array.typeOfAd).indexOf(selectedTypeOfAd))
        bloodTypeSpinner.setSelection(resources.getStringArray(R.array.bloodType).indexOf(selectedBloodType))
        etMinPrice.setText(minPriceInput?.toString())
        etMaxPrice.setText(maxPriceInput?.toString())
        etMinAge.setText(minAgeInput?.toString())
        etMaxAge.setText(maxAgeInput?.toString())

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Filter Ads")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnApply).setOnClickListener {
            selectedCategory = categorySpinner.selectedItem?.toString() ?: "Odaberi Kategoriju"
            selectedRegion = regionSpinner.selectedItem?.toString() ?: "Odaberi Županiju"
            selectedTypeOfAd = typeOfAdSpinner.selectedItem?.toString() ?: "Odaberi Tip Oglasa"
            selectedBloodType = bloodTypeSpinner.selectedItem?.toString() ?: "Odaberi Tip"

            minPriceInput = etMinPrice.text.toString().trim().toDoubleOrNull()
            maxPriceInput = etMaxPrice.text.toString().trim().toDoubleOrNull()
            minAgeInput = etMinAge.text.toString().trim().toIntOrNull()
            maxAgeInput = etMaxAge.text.toString().trim().toIntOrNull()

            applyFilters(selectedCategory, selectedRegion, selectedTypeOfAd, selectedBloodType,
                minPriceInput, maxPriceInput, minAgeInput, maxAgeInput)

            Toast.makeText(requireContext(), "Filters applied", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnRemoveFilters).setOnClickListener {
            selectedCategory = "Odaberi Kategoriju"
            selectedRegion = "Odaberi Županiju"
            selectedTypeOfAd = "Odaberi Tip Oglasa"
            selectedBloodType = "Odaberi Tip"
            minPriceInput = null
            maxPriceInput = null
            minAgeInput = null
            maxAgeInput = null
            resetFilters()
            Toast.makeText(requireContext(), "Filters removed", Toast.LENGTH_SHORT).show()
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

        val filteredAds = adsList.filter { ad ->
            val conditions = mutableListOf<Boolean>()

            if (trimmedCategory != "Odaberi Kategoriju")
                conditions.add(ad.category.trim().equals(trimmedCategory, true))
            if (trimmedRegion != "Odaberi Županiju")
                conditions.add(ad.region.trim().equals(trimmedRegion, true))
            if (trimmedTypeOfAd != "Odaberi Tip Oglasa")
                conditions.add(ad.typeOfAd.trim().equals(trimmedTypeOfAd, true))
            if (trimmedBloodType != "Odaberi Tip")
                conditions.add(ad.bloodType.trim().equals(trimmedBloodType, true))
            minPrice?.let { conditions.add(ad.price >= it) }
            maxPrice?.let { conditions.add(ad.price <= it) }
            minAge?.let { conditions.add(ad.age >= it) }
            maxAge?.let { conditions.add(ad.age <= it) }

            conditions.all { it }
        }

        adsAdapter.updateAds(filteredAds)
        if (filteredAds.isEmpty()) {
            Toast.makeText(requireContext(), "No ads match the selected filters", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetFilters() { fetchAdsFromFirestore() }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
