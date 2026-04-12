package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blueray.marasy.R
import com.blueray.marasy.adapters.MyOrdersPagerAdapter
import com.blueray.marasy.databinding.ActivityMyOrdersBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MyOrdersActivity : BaseActivity() {

    private lateinit var binding: ActivityMyOrdersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (com.blueray.marasy.helpers.HelperUtils.isGuest(this)) {
            com.blueray.marasy.helpers.HelperUtils.showLoginRequiredDialog(this) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            return
        }

        //init bar
        binding.includedTab.title.text = getString(R.string.my_orders)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }


        // Set up the ViewPager2 with the adapter
        val adapter = MyOrdersPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2 using TabLayoutMediator
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.current)
                1 -> getString(R.string.past)
                else -> getString(R.string.cancelled)
            }
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Change the appearance of the selected tab
                tab?.view?.background =
                    ContextCompat.getDrawable(this@MyOrdersActivity, R.drawable.tab_background)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Change the appearance of the unselected tab
                tab?.view?.background =
                    ContextCompat.getDrawable(this@MyOrdersActivity, R.drawable.tab_background)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        for (i in 0 until binding.tabLayout.tabCount) {
            val tab = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val layoutParams = tab.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(8, 8, 8, 8) // Adjust margins as needed
            tab.requestLayout()
        }
    }
}