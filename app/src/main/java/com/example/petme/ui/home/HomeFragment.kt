package com.example.petme.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.njuapp.adapter.ImageSliderAdapter
import com.example.petme.R
import com.example.petme.databinding.FragmentHomeBinding
import com.example.petme.adapters.AdsAdapter
import com.example.petme.ui.ads.adslist.AdsListActivity
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

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
        val recyclerView = binding.recyclerViewListings
        recyclerView.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2) // 2 columns
        recyclerView.layoutManager = gridLayoutManager

        homeViewModel.recommendedAds.observe(viewLifecycleOwner) { ads ->
            // Show only the first 4 ads
            val limitedAds = ads.take(4).toMutableList()  // Convert to MutableList
            val adapter = AdsAdapter(limitedAds)
            recyclerView.adapter = adapter
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up category icons with click listeners
        setUpCategoryIcons()
    }

    private fun setUpCategoryIcons() {
        binding.iconDog.setOnClickListener { openAdsListFragment("dogs") }
        binding.iconCat.setOnClickListener { openAdsListFragment("cats") }
        binding.iconHorse.setOnClickListener { openAdsListFragment("horses") }
        binding.iconSnake.setOnClickListener { openAdsListFragment("reptiles") }
        binding.iconFish.setOnClickListener { openAdsListFragment("fish") }
        binding.iconBird.setOnClickListener { openAdsListFragment("birds") }
        binding.allCategories.setOnClickListener({openAdsListFragment("")})
    }

    private fun openAdsListFragment(category: String) {
        val intent = Intent(requireContext(), AdsListActivity::class.java).apply {
            putExtra(AdsListActivity.EXTRA_CATEGORY, category)
            putExtra("allUsers", "allUsers")// Pass the category
        }
        startActivity(intent)
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

    override fun onDestroyView() {
        handler.removeCallbacks(runnable) // Stop sliding when the fragment is destroyed
        super.onDestroyView()
        _binding = null
    }
}
