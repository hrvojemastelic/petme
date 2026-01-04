package com.example.petme

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.NavHostFragment
import com.example.petme.Constants.EXTRA_SEARCH_QUERY
import com.example.petme.notifications.NotificationsActivity
import com.example.petme.ui.ads.adslist.AdsListFragment

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
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
                handleSearchQuery(query, fromChange = false)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // reagiraj samo ako si već u AdsListFragmentu
                handleSearchQuery(newText, fromChange = true)
                return false
            }

            private fun handleSearchQuery(query: String?, fromChange: Boolean) {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
                val navController = navHostFragment?.navController
                val finalQuery = query?.trim() ?: ""

                if (navController != null) {
                    val currentDest = navController.currentDestination?.id

                    if (currentDest == R.id.adsListFragment) {
                        // samo update kad smo već na listi
                        val fragment = navHostFragment.childFragmentManager.fragments
                            .firstOrNull { it is AdsListFragment } as? AdsListFragment
                        fragment?.updateSearch(finalQuery)
                    } else {
                        // ako nismo na listi → otvaramo samo kod SUBMIT-a
                        if (!fromChange) {
                            val bundle = Bundle().apply {
                                putString("category", "")
                                putString("userId", null)
                                putString(EXTRA_SEARCH_QUERY, finalQuery)
                            }
                            navController.popBackStack(R.id.adsListFragment, true)
                            navController.navigate(R.id.adsListFragment, bundle)
                        }
                    }
                }
            }
        })



        val notificationIcon = customView.findViewById<ImageView>(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            startActivity(android.content.Intent(this, NotificationsActivity::class.java))
        }
    }
}
