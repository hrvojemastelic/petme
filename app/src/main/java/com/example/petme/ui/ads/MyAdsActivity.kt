package com.example.petme.ui.ads

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petme.R
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
        setupActionBar()
        // Fetch and display user's ads
        fetchMyAds()
    }

    private fun setupRecyclerView() {
        adsAdapter = AdsSingleColAdapter(mutableListOf(),showDeleteButton = true) { ad ->
            // Show confirmation dialog before deleting
            AlertDialog.Builder(this)
                .setTitle("Delete Ad")
                .setMessage("Are you sure you want to delete this ad?")
                .setPositiveButton("Yes") { dialog, _ ->
                    deleteAd(ad) // Call delete function
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

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

    private fun deleteAd(ad: ClassifiedAd) {
        firestore.collection("ads").document(ad.id.toString())
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Ad deleted successfully", Toast.LENGTH_SHORT).show()
                adsAdapter.updateAds(adsAdapter.adsList.filter { it.id != ad.id }) // Update list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting ad: ${e.message}", Toast.LENGTH_SHORT).show()
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
    private fun searchForAds(query: String) {
        // Implement search logic here
    }
}
