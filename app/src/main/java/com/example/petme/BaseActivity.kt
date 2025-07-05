// BaseActivity.kt

package com.example.petme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.petme.Constants.EXTRA_SEARCH_QUERY
import com.example.petme.notifications.NotificationsActivity
import com.example.petme.ui.ads.adslist.AdsListActivity

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

        // 👇 THIS sends the query to AdsListActivity
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        val intent = Intent(this@BaseActivity, AdsListActivity::class.java)
                        intent.putExtra(EXTRA_SEARCH_QUERY, it.trim())
                        startActivity(intent)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true // we don’t care about live typing here
            }
        })

        val notificationIcon = customView.findViewById<ImageView>(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
    }
}
