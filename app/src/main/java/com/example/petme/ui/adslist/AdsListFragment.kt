package com.example.petme.ui.ads.adslist

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petme.Constants.EXTRA_SEARCH_QUERY
import com.example.petme.R
import com.example.petme.adapters.AdsSingleColAdapter
import com.example.petme.databinding.FragmentAdsListBinding
import com.example.petme.models.ClassifiedAd
import com.example.petme.ui.home.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore

class AdsListFragment : Fragment() {

    private var selectedCategory: String = "Odaberi Kategoriju"
    private var selectedRegion: String = "Odaberi Županiju"
    private var selectedTypeOfAd: String = "Odaberi Tip Oglasa"
    private var selectedBloodType: String = "Odaberi Tip"
    private var minPriceInput: Double? = null
    private var maxPriceInput: Double? = null
    private var minAgeInput: Int? = null
    private var maxAgeInput: Int? = null

    private var _binding: FragmentAdsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adsAdapter: AdsSingleColAdapter
    private var adsList = mutableListOf<ClassifiedAd>()

    private var category: String = ""
    private var userId: String? = null
    private var query: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            category = it.getString("category", "")
            userId = it.getString("userId")
            query = it.getString(EXTRA_SEARCH_QUERY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        firestore = FirebaseFirestore.getInstance()

        binding.recyclerViewAds.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAds.setHasFixedSize(true)
        binding.recyclerViewAds.clipToPadding = false

        adsAdapter = AdsSingleColAdapter(mutableListOf(), showDeleteButton = false) {}
        binding.recyclerViewAds.adapter = adsAdapter

        val sortOptions = resources.getStringArray(R.array.sort_options)
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, sortOptions
        )
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

        binding.btnFilter.setOnClickListener { showFilterDialog() }

        if (userId.isNullOrBlank()) {
            fetchAdsFromFirestore()
        } else {
            fetchAdsByUserId(userId!!)
        }

        val searchView = requireActivity().findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(null)
        searchView.setQuery(query ?: "", false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean {
                searchForAds(q.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchForAds(newText.orEmpty())
                return true
            }
        })
    }

    private fun fetchAdsFromFirestore() {
        firestore.collection("ads")
            .get()
            .addOnSuccessListener { result ->
                adsList.clear()
                for (document in result) {
                    val ad = document.toObject(ClassifiedAd::class.java)
                    adsList.add(ad)
                }
                val filteredAds = if (category.isEmpty()) adsList
                else adsList.filter { it.category.equals(category, ignoreCase = true) }

                adsAdapter.updateAds(filteredAds)
                query?.takeIf { it.isNotBlank() }?.let { searchForAds(it) }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri dohvaćanju oglasa", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAdsByUserId(userId: String) {
        firestore.collection("ads")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                adsList.clear()
                for (document in documents) {
                    val ad = document.toObject(ClassifiedAd::class.java)
                    adsList.add(ad)
                }
                adsAdapter.updateAds(adsList)
                query?.takeIf { it.isNotBlank() }?.let { searchForAds(it) }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri dohvaćanju oglasa", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchForAds(query: String) {
        val filtered = adsList.filter {
            it.title.contains(query, true) ||
                    it.description.contains(query, true) ||
                    it.breed.contains(query, true)
        }
        adsAdapter.updateAds(filtered)

        if (filtered.isEmpty()) {
            Toast.makeText(requireContext(), "Nema rezultata za \"$query\"", Toast.LENGTH_SHORT).show()
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
            .setTitle("Filter oglasa")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnApply).setOnClickListener {
            selectedCategory = categorySpinner.selectedItem.toString()
            selectedRegion = regionSpinner.selectedItem.toString()
            selectedTypeOfAd = typeOfAdSpinner.selectedItem.toString()
            selectedBloodType = bloodTypeSpinner.selectedItem.toString()
            minPriceInput = etMinPrice.text.toString().toDoubleOrNull()
            maxPriceInput = etMaxPrice.text.toString().toDoubleOrNull()
            minAgeInput = etMinAge.text.toString().toIntOrNull()
            maxAgeInput = etMaxAge.text.toString().toIntOrNull()

            applyFilters(
                selectedCategory,
                selectedRegion,
                selectedTypeOfAd,
                selectedBloodType,
                minPriceInput,
                maxPriceInput,
                minAgeInput,
                maxAgeInput
            )
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
            fetchAdsFromFirestore()
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
        val filteredAds = adsList.filter { ad ->
            val checks = mutableListOf<Boolean>()

            if (category != "Odaberi Kategoriju") checks.add(ad.category.equals(category, true))
            if (region != "Odaberi Županiju") checks.add(ad.region.equals(region, true))
            if (typeOfAd != "Odaberi Tip Oglasa") checks.add(ad.typeOfAd.equals(typeOfAd, true))
            if (bloodType != "Odaberi Tip") checks.add(ad.bloodType.equals(bloodType, true))
            minPrice?.let { checks.add(ad.price >= it) }
            maxPrice?.let { checks.add(ad.price <= it) }
            minAge?.let { checks.add(ad.age >= it) }
            maxAge?.let { checks.add(ad.age <= it) }

            checks.all { it }
        }

        adsAdapter.updateAds(filteredAds)
        if (filteredAds.isEmpty()) {
            Toast.makeText(requireContext(), "Nema rezultata za odabrane filtere", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
