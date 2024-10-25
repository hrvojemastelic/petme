package com.example.petme

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.petme.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val actionBar = supportActionBar
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false) // Disable default title
        val customView = layoutInflater.inflate(R.layout.custom_action_bar, null)
        actionBar?.customView = customView

        val searchView = customView.findViewById<SearchView>(R.id.searchView)

        // Keep the SearchView expanded
        searchView.setIconifiedByDefault(false)
        searchView.clearFocus()  // Ensure it doesn't focus automatically

        // Handle search query submission
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search query submission
                if (query != null) {
                    // Perform the search with the query
                    searchForAds(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle search text change, if needed
                return true
            }
        })


        // Set up Action Bar with NavController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Listen for destination changes to update the Action Bar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.adsListFragment -> supportActionBar?.setDisplayHomeAsUpEnabled(true)
                else -> supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        }
    }


    private fun searchForAds(query: String) {
        // Implement search logic here (e.g., filter ads)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
