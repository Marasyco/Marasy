package com.blueray.marasy.ui.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityHomeBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.helpers.HelperUtils.isGuest
import com.blueray.marasy.helpers.HelperUtils.showLoginRequiredDialog
import com.blueray.marasy.ui.activities.LoginActivity
import com.blueray.marasy.ui.driver.DriverHomeActivity
import com.blueray.marasy.ui.shopper.ShopperHomeActivity

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is a driver or shopper and redirect accordingly
        val role = HelperUtils.getRole(this)
        if (role == "driver") {
            startActivity(Intent(this, DriverHomeActivity::class.java))
            finishAffinity()
            return
        } else if (role == "shoper") {
            startActivity(Intent(this, ShopperHomeActivity::class.java))
            finishAffinity()
            return
        }
        
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.ordersButton.setOnClickListener {
            if (HelperUtils.isGuest(this)) {
                HelperUtils.showLoginRequiredDialog(this) {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            } else {
                val intent = Intent(this, MyOrdersActivity::class.java)
                startActivity(intent)
            }
        }

        binding.bottomBar.onItemSelected = { index ->
            navigateToBottomBarDestination(index)
        }
        
        binding.bottomBar.onItemReselected = { index ->
            // Also handle reselection (clicking same item again)
            navigateToBottomBarDestination(index)
        }

        setUpDrawerNavigation()
    }
    
    private fun navigateToBottomBarDestination(index: Int) {
        when (index) {
            0 -> {
                // Navigate to home
                try {
                    navController.navigate(R.id.homeFragment, null, 
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, true)
                            .build()
                    )
                } catch (e: Exception) {
                    navController.popBackStack(R.id.homeFragment, false)
                }
            }

            1 -> {
                try {
                    navController.navigate(R.id.allCategoriesFragment, null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .build()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            3 -> {
                if (HelperUtils.isGuest(this)) {
                    HelperUtils.showLoginRequiredDialog(this) {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    try {
                        navController.navigate(R.id.favoriteFragment, null,
                            androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.homeFragment, false)
                                .build()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            4 -> {
                try {
                    navController.navigate(R.id.contactUsActivity, null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .build()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun setUpDrawerNavigation() {
        binding.navDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.homeFragment, null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, true)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                    closeDrawer()
                    true
                }

                R.id.search -> {
                    navController.navigate(R.id.searchFragment, null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                    closeDrawer()
                    true
                }

                R.id.favorite -> {
                    navController.navigate(R.id.favoriteFragment, null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                    closeDrawer()
                    true
                }

                R.id.notifications -> {
                    if (HelperUtils.isGuest(this)) {
                        HelperUtils.showLoginRequiredDialog(this) {
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        navController.navigate(R.id.notificationsFragment, null,
                            androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.homeFragment, false)
                                .setLaunchSingleTop(true)
                                .build()
                        )
                    }
                    closeDrawer()
                    true
                }

                R.id.profile -> {
                    if (HelperUtils.isGuest(this)) {
                        HelperUtils.showLoginRequiredDialog(this) {
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        val intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)
                    }
                    closeDrawer()
                    true
                }

                R.id.aboutUs -> {
                    val intent = Intent(this, AboutUsActivity::class.java)
                    startActivity(intent)
                    closeDrawer()
                    true
                }


                R.id.warranties -> {
                    val intent = Intent(this, WarrantiesActivity::class.java)
                    startActivity(intent)
                    closeDrawer()
                    true
                }

                R.id.privacyPolicy -> {
                    val intent = Intent(this, PrivacyActivity::class.java)
                    startActivity(intent)
                    closeDrawer()
                    true
                }

                R.id.contactUs -> {
                    navController.navigate(R.id.contactUsActivity, null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                    closeDrawer()
                    true
                }

                R.id.language -> {
                    val intent = Intent(this, ChangeLanguageActivity::class.java)
                    startActivity(intent)
                    closeDrawer()
                    true
                }


                R.id.logout -> {
                    if (HelperUtils.isGuest(this)) {
                        // If guest, go directly to login page
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        // If logged in, show logout confirmation dialog
                        showLogoutDialog(this)
                    }
                    closeDrawer()
                    true
                }


                else -> {
                    false
                }
            }
        }
    }


    private fun showLogoutDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(false) // Prevent closing on outside touch
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            dialog.dismiss()
            val sharedPreferences =
                getSharedPreferences(HelperUtils.SHARED_PREF, MODE_PRIVATE)

            sharedPreferences.edit().apply {
                putString("uid", "0")

                putString("role", "0")

            }.apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}