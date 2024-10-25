package com.example.petme.ui.ads

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petme.adapters.AdsSingleColAdapter
import com.example.petme.databinding.ActivityMyAdsBinding
import com.example.petme.models.ClassifiedAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyAdsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAdsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adsAdapter: AdsSingleColAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()

        // Fetch and display user's ads
        fetchMyAds()
    }

    private fun setupRecyclerView() {
        adsAdapter = AdsSingleColAdapter(mutableListOf())
        binding.recyclerViewMyAds.apply {
            layoutManager = LinearLayoutManager(this@MyAdsActivity)
            adapter = adsAdapter
        }
    }

    private fun fetchMyAds() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("ads")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val userAds = documents.mapNotNull { it.toObject(ClassifiedAd::class.java) }
                    adsAdapter.updateAds(userAds)
                }
                .addOnFailureListener { exception ->
                    Log.e("MyAdsActivity", "Error retrieving ads: ${exception.message}")
                    Toast.makeText(this, "Failed to load ads", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please log in to view your ads", Toast.LENGTH_SHORT).show()
        }
    }
}