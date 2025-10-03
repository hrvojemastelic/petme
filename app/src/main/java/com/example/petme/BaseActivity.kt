package com.example.petme

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.NavHostFragment
import com.example.petme.Constants.EXTRA_SEARCH_QUERY
import com.example.petme.notifications.NotificationsActivity

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
                query?.let {
                    if (it.isNotBlank()) {
                        val navHostFragment =
                            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
                        val navController = navHostFragment?.navController

                        if (navController != null) {
                            val bundle = Bundle().apply {
                                putString("category", "")
                                putString("userId", null)
                                putString(EXTRA_SEARCH_QUERY, it.trim())
                            }
                            navController.navigate(R.id.adsListFragment, bundle)
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        val notificationIcon = customView.findViewById<ImageView>(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            startActivity(android.content.Intent(this, NotificationsActivity::class.java))
        }
    }
}
