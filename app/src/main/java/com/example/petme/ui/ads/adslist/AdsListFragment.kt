package com.example.petme.ui.ads.adslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petme.R
import com.example.petme.databinding.FragmentAdsListBinding
import com.example.petme.models.ClassifiedAd
import com.example.petme.adapters.AdsAdapter
import com.example.petme.adapters.AdsSingleColAdapter
import com.example.petme.ui.home.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore

class AdsListFragment : Fragment() {

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String): AdsListFragment {
            val fragment = AdsListFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentAdsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var category: String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adsAdapter: AdsSingleColAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdsListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Get the passed category (like "dogs", "cats")
        category = arguments?.getString(ARG_CATEGORY) ?: "All"

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        val recyclerView = binding.recyclerViewAds
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        // Set up sort dropdown (Spinner)
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val spinner: Spinner = binding.spinnerSort
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sortOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set up Filter Button
        binding.btnFilter.setOnClickListener {
            Toast.makeText(requireContext(), "Filter clicked", Toast.LENGTH_SHORT).show()
            // Implement filter functionality here
        }

        // Initialize adapter for the RecyclerView
        adsAdapter = AdsSingleColAdapter(mutableListOf()){} // Pass an empty mutable list

        recyclerView.adapter = adsAdapter

        // Fetch Ads from Firestore
        fetchAdsFromFirestore()

        return root
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
                Toast.makeText(requireContext(), "Error fetching ads: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
