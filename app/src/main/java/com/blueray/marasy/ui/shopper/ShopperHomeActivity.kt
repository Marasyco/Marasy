package com.blueray.marasy.ui.shopper

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.ViewPagerAdapter
import com.blueray.marasy.databinding.ActivityShopperHomeBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.helpers.HelperUtils.showConfirmDialog
import com.blueray.marasy.ui.activities.BaseActivity
import com.blueray.marasy.ui.activities.HomeActivity
import com.blueray.marasy.ui.activities.LoginActivity
import com.blueray.marasy.ui.driver.DriverHomeActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout

class ShopperHomeActivity : BaseActivity() {
    private lateinit var binding: ActivityShopperHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is not a shopper and redirect accordingly
        val role = HelperUtils.getRole(this)
        if (role != "shoper") {
            if (role == "driver") {
                startActivity(Intent(this, DriverHomeActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            finishAffinity()
            return
        }
        
        binding = ActivityShopperHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.includedTab.title.text = getString(R.string.orders)

        binding.includedTab.backButton.setOnClickListener {
            showConfirmDialog(this, getString(R.string.are_you_sure_you_want_to_logout)) {
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

        }

        val viewPager: ViewPager = binding.viewPager
        setupViewPager(viewPager)

        val tabLayout: TabLayout = binding.tabLayout
        tabLayout.setupWithViewPager(viewPager)

        setupCustomTabs(tabLayout)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(
            ShopperOrdersFragment.newInstance(1),
            getString(R.string.waiting_collect)
        )
        adapter.addFragment(ShopperOrdersFragment.newInstance(2), getString(R.string.collected))


        viewPager.adapter = adapter
    }

    private fun setupCustomTabs(tabLayout: TabLayout) {
        val titles = listOf(getString(R.string.waiting_collect), getString(R.string.collected))

        for (i in titles.indices) {
            val tab = tabLayout.getTabAt(i)
            val customView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null)
            val textView = customView.findViewById<TextView>(R.id.tabText)
            textView.text = titles[i]
            tab?.customView = customView

            // Set initial selected state for first tab
            if (i == 0) {
                setTabSelectedState(customView, true)
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.let { setTabSelectedState(it, true) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.let { setTabSelectedState(it, false) }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setTabSelectedState(view: View, isSelected: Boolean) {
        val textView = view.findViewById<TextView>(R.id.tabText)
        val cardView = view.findViewById<MaterialCardView>(R.id.cardView)

        if (isSelected) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            cardView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.orange))
            cardView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        }
    }


}