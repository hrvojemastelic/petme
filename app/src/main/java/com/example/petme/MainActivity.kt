package com.example.petme

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.petme.databinding.ActivityMainBinding
import com.example.petme.ui.user.auth.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        setupNavigation(navView, navController)

        // 🔽 ovdje sakrivaš strelicu
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Check authentication state and fetch user data
        authViewModel.checkAuthState()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                println("FCM Token: $token")
            } else {
                println("Fetching FCM registration token failed: ${task.exception}")
            }
        }
    }

    private fun setupNavigation(navView: BottomNavigationView, navController: NavController) {
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_addad, R.id.user)
        )
        navView.setupWithNavController(navController)

        // 🔽 Dodano da se stack čisti kad biraš root item
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(
                        R.id.navigation_home,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.navigation_home, inclusive = false)
                            .build()
                    )
                    true
                }
                R.id.navigation_addad -> {
                    navController.navigate(
                        R.id.navigation_addad,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.navigation_addad, inclusive = false)
                            .build()
                    )
                    true
                }
                R.id.user -> {
                    navController.navigate(
                        R.id.user,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.user, inclusive = false)
                            .build()
                    )
                    true
                }
                else -> false
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Notifications", "POST_NOTIFICATIONS permission granted")
            } else {
                Log.d("Notifications", "POST_NOTIFICATIONS permission denied")
            }
        }
    }
}
