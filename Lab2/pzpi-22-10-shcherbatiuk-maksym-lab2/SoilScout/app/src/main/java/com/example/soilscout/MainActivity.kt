package com.example.soilscout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.soilscout.databinding.ActivityMainBinding
import com.example.soilscout.ui.profile.ProfileActivity
import com.example.soilscout.ui.profile.ProfileViewModel
import com.example.soilscout.ui.profile.ResultWrapper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.soilscout.util.Constants
import com.example.soilscout.util.LocaleManager
import com.example.soilscout.ui.profile.ProfileUpdateListener
import com.example.soilscout.model.User // ДОДАНО: Імпорт класу User

class MainActivity : AppCompatActivity(), ProfileUpdateListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val profileViewModel: ProfileViewModel by viewModels()

    private var currentProfileImageUrl: String? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        profileViewModel.userProfile.observe(this, Observer { result ->
            when (result) {
                is ResultWrapper.Loading -> {
                    Log.d("MainActivity", "Fetching user profile in MainActivity: Loading...")
                }
                is ResultWrapper.Success -> {
                    Log.d("MainActivity", "User profile fetched successfully in MainActivity. User ID: ${result.data.id}, Photo URL: ${result.data.profile_picture_url}")
                    currentProfileImageUrl = result.data.profile_picture_url
                    invalidateOptionsMenu()
                }
                is ResultWrapper.Error -> {
                    Log.e("MainActivity", "Failed to fetch user profile in MainActivity: ${result.message}")
                    currentProfileImageUrl = null
                    invalidateOptionsMenu()
                }
            }
        })

        profileViewModel.fetchUserProfile()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_action_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.d("MainActivity", "onPrepareOptionsMenu called in MainActivity.")
        val profileItem = menu?.findItem(R.id.action_profile)

        if (profileItem != null) {
            if (!currentProfileImageUrl.isNullOrEmpty()) {
                val imageUrl = "${Constants.BASE_IMAGE_URL}${currentProfileImageUrl}"
                Log.d("MainActivity", "Attempting to load profile icon from URL: $imageUrl")

                Glide.with(this)
                    .asDrawable()
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            Log.d("MainActivity", "Profile icon loaded successfully from URL: $imageUrl")
                            profileItem.icon = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            Log.d("MainActivity", "Profile icon load cleared for URL: $imageUrl")
                            profileItem.icon = placeholder
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            Log.e("MainActivity", "Profile icon load FAILED for URL: $imageUrl")
                            profileItem.icon = errorDrawable ?: resources.getDrawable(R.drawable.ic_menu_gallery, theme)
                        }
                    })
            } else {
                Log.d("MainActivity", "No profile image URL, setting default icon.")
                profileItem.setIcon(R.drawable.ic_menu_gallery)
            }
        } else {
            Log.w("MainActivity", "Action bar profile menu item not found.")
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val profileIntent = Intent(this, ProfileActivity::class.java)
                startActivity(profileIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onProfileDataUpdated(user: User) {
        Log.d("MainActivity", "onProfileDataUpdated received in MainActivity.")
        currentProfileImageUrl = user.profile_picture_url
        invalidateOptionsMenu()
    }
}