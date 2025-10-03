package com.example.petme.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.njuapp.adapter.ImageSliderAdapter
import com.example.petme.Constants.EXTRA_SEARCH_QUERY
import com.example.petme.MainActivity
import com.example.petme.R
import com.example.petme.databinding.FragmentHomeBinding
import com.example.petme.adapters.AdsAdapter
import com.example.petme.adapters.AdsHorizontalAdapter
import com.example.petme.models.ClassifiedAd
import com.example.petme.ui.ads.adslist.AdsListActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var firestore: FirebaseFirestore
    private var adsList = mutableListOf<ClassifiedAd>()

    private lateinit var recyclerView : RecyclerView
    private lateinit var recyclerViewLiveStock : RecyclerView
    private lateinit var recyclerViewHorse : RecyclerView
    private lateinit var recyclerViewRecommended : RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        firestore = FirebaseFirestore.getInstance()
        // Set up ViewPager2 for Slideshow
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        homeViewModel.slideshowImages.observe(viewLifecycleOwner) { images ->
            val adapter = ImageSliderAdapter(requireContext(), images)
            viewPager.adapter = adapter

            // Set up TabLayout with ViewPager2
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                // Optional: Set tab text or icon if needed
                // tab.text = "Tab ${position + 1}"
            }.attach()
            // Start automatic sliding
            startAutoSliding(viewPager, images.size)
        }

        // Set up RecyclerView for Recommended Listings with GridLayoutManager (2 columns)
        recyclerView = binding.recyclerViewListings
        recyclerViewLiveStock = binding.liveStockAds
        recyclerViewHorse = binding.recyclerviewKonji
        recyclerViewRecommended = binding.recyclerviewPreporuceni

        recyclerView.setHasFixedSize(true)
        recyclerViewLiveStock.setHasFixedSize(true)
        recyclerViewHorse.setHasFixedSize(true)
        recyclerViewRecommended.setHasFixedSize(true)

        val lineraLayout = LinearLayoutManager(requireContext())
        lineraLayout.orientation = LinearLayoutManager.HORIZONTAL

        val lineraLayoutHorse = LinearLayoutManager(requireContext())
        lineraLayoutHorse.orientation = LinearLayoutManager.HORIZONTAL

        val lineraLayoutRecommended = LinearLayoutManager(requireContext())
        lineraLayoutRecommended.orientation = LinearLayoutManager.HORIZONTAL

        val gridLayoutManager = GridLayoutManager(requireContext(), 2) // 2 columns

        recyclerView.layoutManager = gridLayoutManager
        recyclerViewLiveStock.layoutManager = lineraLayout
        recyclerViewHorse.layoutManager = lineraLayoutHorse
        recyclerViewRecommended.layoutManager = lineraLayoutRecommended
       /* homeViewModel.recommendedAds.observe(viewLifecycleOwner) { ads ->
            // Show only the first 4 ads
            val limitedAds = ads.take(4).toMutableList()  // Convert to MutableList
            val adapter = AdsAdapter(limitedAds)
            recyclerView.adapter = adapter
        }*/
        fetcDoghAdsFromFirestore()

        val showAllC = binding.showAllCategories
        val showAllCategoriesText = binding.showAllCategoriesText
        val hiddenCategory1 = binding.livestock
        val hiddenCategory2 = binding.rodents
        val hiddenCategory3 = binding.lostandfound

        showAllC.setOnClickListener{
            if (hiddenCategory1.visibility == View.GONE) {
                hiddenCategory1.visibility = View.VISIBLE
                hiddenCategory2.visibility = View.VISIBLE
                hiddenCategory3.visibility = View.VISIBLE
                showAllCategoriesText.text = "Sakrij  kategorije"
            } else {
                hiddenCategory1.visibility = View.GONE
                hiddenCategory2.visibility = View.GONE
                hiddenCategory3.visibility = View.GONE
                showAllCategoriesText.text = "Prikaži više kategorija"
            }

        }
        showAllCategoriesText.setOnClickListener {
            if (hiddenCategory1.visibility == View.GONE) {
                hiddenCategory1.visibility = View.VISIBLE
                hiddenCategory2.visibility = View.VISIBLE
                hiddenCategory3.visibility = View.VISIBLE
                showAllCategoriesText.text = "Sakrij kategorije"
            } else {
                hiddenCategory1.visibility = View.GONE
                hiddenCategory2.visibility = View.GONE
                hiddenCategory3.visibility = View.GONE
                showAllCategoriesText.text = "Prikaži više kategorija"
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up category icons with click listeners
        setUpCategoryIcons()
    }

    private fun setUpCategoryIcons() {
        binding.dogs.setOnClickListener { openAdsListFragment("psi") }
        binding.cats.setOnClickListener { openAdsListFragment("mačke") }
        binding.horses.setOnClickListener { openAdsListFragment("konji") }
        binding.reptails.setOnClickListener { openAdsListFragment("reptili") }
        binding.fish.setOnClickListener { openAdsListFragment("riba") }
        binding.birds.setOnClickListener { openAdsListFragment("ptice") }
        binding.livestock.setOnClickListener { openAdsListFragment("stoka") }
        binding.rodents.setOnClickListener { openAdsListFragment("glodavci") }
        binding.lostandfound.setOnClickListener { openAdsListFragment("izgubljeno/nađeno") }

        binding.allCategories.setOnClickListener({openAdsListFragment("")})
    }

    private fun openAdsListFragment(category: String) {
        Log.d("category" ,category.toString())
        val bundle = Bundle().apply {
            putString("category", category)
            putString("userId", null)
            putString(EXTRA_SEARCH_QUERY, "")
        }
        findNavController().navigate(R.id.adsListFragment, bundle)
    }

    private fun startAutoSliding(viewPager: ViewPager2, totalImages: Int) {
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            val nextItem = if (viewPager.currentItem == totalImages - 1) {
                0
            } else {
                viewPager.currentItem + 1
            }
            viewPager.setCurrentItem(nextItem, true)
            handler.postDelayed(runnable, 3000) // Change image every 3 seconds
        }
        handler.postDelayed(runnable, 3000) // Initial delay
    }


    private fun fetcDoghAdsFromFirestore() {
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
                val filteredAdsLostFound = adsList.filter { it.category.equals("Izgubljeno/Nađeno", ignoreCase = true) }
                val filterAdsLiveStock  = adsList.filter { it.category.equals("Blago/Stoka", ignoreCase = true) }
                val filterAdsHorse  = adsList.filter { it.category.equals("Konji", ignoreCase = true) }
                val filterRecommended = adsList.filter { it.category.equals("Blago/Stoka", ignoreCase = true) }

                val limiteLostFound = filteredAdsLostFound.take(4).toMutableList()  // Convert to MutableList
                val adapterLF = AdsAdapter(limiteLostFound)
                recyclerView.adapter = adapterLF

                val limitedAdsLiveStock = filterAdsLiveStock.take(8).toMutableList() // Convert to MutableList
                val adapterLiveStock = AdsHorizontalAdapter(limitedAdsLiveStock)
                recyclerViewLiveStock.adapter = adapterLiveStock

                val limitedHorses = filterAdsHorse.take(8).toMutableList() // Convert to MutableList
                val adapterHorses = AdsHorizontalAdapter(limitedHorses)
                recyclerViewHorse.adapter = adapterHorses

                val limitedRecommended = filterRecommended.take(8).toMutableList()  // Convert to MutableList
                val adapterRecommended = AdsHorizontalAdapter(limitedRecommended)
                recyclerViewRecommended.adapter = adapterRecommended
            }
            .addOnFailureListener { exception ->
                System.out.println("exception on dogs list in home fragment" + exception.message.toString())
            }
    }

    override fun onDestroyView() {
        handler.removeCallbacks(runnable) // Stop sliding when the fragment is destroyed
        super.onDestroyView()
        _binding = null
    }
}
