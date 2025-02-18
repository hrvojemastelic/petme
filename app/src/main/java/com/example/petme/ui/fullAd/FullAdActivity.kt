package com.example.petme.ui.fullAd

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.petme.R
import com.example.petme.adapters.AdsAdapter
import com.example.petme.adapters.ImagePagerAdapter
import com.example.petme.databinding.ActivityFullAdBinding
import com.example.petme.models.ClassifiedAd
import com.example.petme.session.UserSession
import com.example.petme.ui.ads.MyAdsActivity
import com.example.petme.ui.ads.adslist.AdsListActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class FullAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullAdBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        val adId = intent.getStringExtra("adId") // Get adId as String
        val userId = intent.getStringExtra("userId")
        Log.d("fulladddd",UserSession.username.toString())
        val username = UserSession.username.toString()


        binding.ostaliOglasiKorsinika.setText(username);

        binding.allAds.setOnClickListener()
        {
            val intent = Intent(this, AdsListActivity::class.java).apply {
                putExtra("userId", userId)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // Add the FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)

        }
        Log.d("userId",userId.toString())
        fetchAdsByUserId(userId.toString())

        if (adId != null) {
            fetchAdDetails(adId) // Fetch and display ad details
        } else {
            Toast.makeText(this, "Ad not found", Toast.LENGTH_SHORT).show()
            finish() // Close activity if adId is missing
        }

        recyclerView = binding.recyclerViewwUserAads
        recyclerView.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(this, 2) // 2 columns
        recyclerView.layoutManager = gridLayoutManager
    }

    private fun fetchAdDetails(adId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("ads").document(adId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val ad = document.toObject(ClassifiedAd::class.java)
                    Log.d("ad full", ad.toString())
                    ad?.let { displayAdDetails(it) }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load ad details", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun displayAdDetails(ad: ClassifiedAd) {
        findViewById<TextView>(R.id.priceTextView).text = "$${ad.price}"
        findViewById<TextView>(R.id.titleTextView).text = ad.title
        findViewById<TextView>(R.id.descriptionTextView).text = ad.description
        findViewById<TextView>(R.id.age).text = "Age: ${ad.age}"
        findViewById<TextView>(R.id.breedTextView).text = ad.breed
        //Convert date
        val date = ad.date.toDate()
        // Format Date
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        findViewById<TextView>(R.id.date).text = formattedDate

        findViewById<TextView>(R.id.phoneNumber).text = "${ad.phoneNumber}"

        findViewById<TextView>(R.id.address).text =ad.address
        findViewById<TextView>(R.id.region).text = ad.region
        findViewById<TextView>(R.id.category).text = ad.category



        // Setup ViewPager with image URLs
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        viewPager.adapter = ImagePagerAdapter(this, ad.imageUrls)
    }

    private fun setupImageSlider(imageUrls: List<String>) {
        // Setup image slider here, or use a library for viewing images fullscreen on click
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
                val list =  adList.take(4).toMutableList()
                val adapter = AdsAdapter(list)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(recyclerView.context, "Error fetching ads: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
