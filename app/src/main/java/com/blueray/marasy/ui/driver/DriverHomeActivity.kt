package com.blueray.marasy.ui.driver

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.ViewPagerAdapter
import com.blueray.marasy.databinding.ActivityDriverHomeBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.helpers.HelperUtils.showConfirmDialog
import com.blueray.marasy.ui.activities.BaseActivity
import com.blueray.marasy.ui.activities.HomeActivity
import com.blueray.marasy.ui.activities.LoginActivity
import com.blueray.marasy.ui.shopper.ShopperHomeActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout

class DriverHomeActivity : BaseActivity() {
    private lateinit var binding: ActivityDriverHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is not a driver and redirect accordingly
        val role = HelperUtils.getRole(this)
        if (role != "driver") {
            if (role == "shoper") {
                startActivity(Intent(this, ShopperHomeActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            finishAffinity()
            return
        }
        
        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
//        tabLayout.getTabAt(0)?.select() // Optional: Force first tab selected appearance


    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(
            DriverOrdersFragment.newInstance(1),
            getString(R.string.waiting_delivery)
        )
        adapter.addFragment(UnderDeliveryFragment.newInstance(2), getString(R.string.delivering2))
        adapter.addFragment(DeliveredOrdersFragment.newInstance(3), getString(R.string.delivered2))


        viewPager.adapter = adapter
    }

    private fun setupCustomTabs(tabLayout: TabLayout) {
        val titles = listOf(
            getString(R.string.waiting_delivery),
            getString(R.string.delivering2),
            getString(R.string.delivered2)
        )

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
            textView.setTextColor(ContextCompat.getColor(this, R.color.orange))
            cardView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            cardView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
        }
    }


}