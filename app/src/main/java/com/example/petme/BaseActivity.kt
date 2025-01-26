package com.example.petme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.startActivity
import com.example.petme.R
import com.example.petme.notifications.NotificationsActivity

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the custom action bar in every activity that extends this class
        setupActionBar()
    }

    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)

        // Inflate custom action bar layout
        val customView = layoutInflater.inflate(R.layout.custom_action_bar, null)
        actionBar?.customView = customView

        // Setup SearchView
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

        // Set OnClickListener for notification icon
        val notificationIcon = customView.findViewById<ImageView>(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            // Open NotificationsActivity when notification icon is clicked
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun searchForAds(query: String) {
        // Your search functionality here
        // Example: Show ads based on the search query
    }
}
