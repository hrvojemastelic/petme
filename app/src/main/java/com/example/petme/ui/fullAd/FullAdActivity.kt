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
import com.example.petme.BaseActivity
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

class FullAdActivity : BaseActivity() {

    private lateinit var binding: ActivityFullAdBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerView : RecyclerView
    private var _username:String = "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adId = intent.getStringExtra("adId") // Get adId as String
        val userId = intent.getStringExtra("userId")

        fetchUsernameByUserId(userId.toString())

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

    fun fetchUsernameByUserId(userId: String): String {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    _username = document.getString("username").toString()
                    binding.ostaliOglasiKorsinika.setText(_username);

                    Log.d("FullAd",_username)
                } else {
                    _username = "" // Handle no user found
                    binding.ostaliOglasiKorsinika.setText("unknown");

                    Log.d("FullAd",_username)

                }
            }
            .addOnFailureListener { exception ->
                _username = "null" // Handle failure
                Log.d("FullAd",_username)

                exception.printStackTrace()
            }
        return _username;
    }
}
