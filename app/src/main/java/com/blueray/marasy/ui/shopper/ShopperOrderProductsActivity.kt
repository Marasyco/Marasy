package com.blueray.marasy.ui.shopper

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
import com.blueray.marasy.databinding.ActivityShopperOrderProductsBinding
import com.blueray.marasy.ui.activities.BaseActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout

class ShopperOrderProductsActivity : BaseActivity() {
    private lateinit var binding: ActivityShopperOrderProductsBinding
    var orderId = ""
    var tid = ""
    var category = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopperOrderProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getStringExtra("orderId").toString()
        tid = intent.getStringExtra("tid").toString()
        category = intent.getStringExtra("category").toString()

        binding.includedTab.title.text = getString(R.string.order_details)
        binding.includedTab.backButton.setOnClickListener {
            finish()
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
            ShopperOrderProductsFragment.newInstance(1, orderId,  category),
            getString(R.string.products)
        )
        adapter.addFragment(
            ShopperOrderProductsFragment.newInstance(2, orderId,  category),
            getString(R.string.done)
        )


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
            textView.setTextColor(ContextCompat.getColor(this, R.color.orange))
            cardView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            cardView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
        }
    }
}