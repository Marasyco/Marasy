package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityProductsBinding
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.model.SubCategoriesData
import com.blueray.marasy.ui.fragments.ProductsListFragment
import com.blueray.marasy.viewmodel.AppViewModel

class ProductsActivity : BaseActivity() {

    private lateinit var binding: ActivityProductsBinding
    private val viewmodel by viewModels<AppViewModel>()

    private var categoryName = ""
    private var categoryId = ""
    private var subCategoryId: String? = null
    private var hasChild: Boolean = false
    
    // Trademark support
    private var isFromTrademark: Boolean = false
    private var trademarkId: String? = null
    private var trademarkName: String? = null

    private lateinit var pagerAdapter: SubSubCategoriesPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        categoryName = intent.getStringExtra("categoryName").toString()
        categoryId = intent.getStringExtra("categoryId").toString()
        subCategoryId = intent.getStringExtra("subCategoryId")
        hasChild = intent.getBooleanExtra("hasChild", false)
        
        // Check if coming from trademark
        isFromTrademark = intent.getBooleanExtra("isFromTrademark", false)
        trademarkId = intent.getStringExtra("trademarkId")
        trademarkName = intent.getStringExtra("trademarkName")

        binding.includedTab.title.text = if (isFromTrademark) trademarkName else categoryName
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }

        // Show cart icon and handle navigation
        binding.includedTab.cartButton.visibility = android.view.View.VISIBLE
        binding.includedTab.cartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        if (isFromTrademark && trademarkId != null) {
            // Show products filtered by trademark
            showProducts()
        } else if (hasChild && subCategoryId != null) {
            // Show tabs with 3rd level subcategories
            showTabsWithSubCategories()
        } else {
            // Show products directly
            showProducts()
        }
    }

    private fun showTabsWithSubCategories() {
        binding.subTabLayout.isVisible = true
        binding.subViewPager.isVisible = true
        binding.productsContainer.isGone = true

        viewmodel.retrieveSubCategories(subCategoryId!!)
        observeSubSubCategories()
    }

    private fun observeSubSubCategories() {
        viewmodel.getSubCategories().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    val list = result.data.data
                    if (list.isNotEmpty()) {
                        pagerAdapter = SubSubCategoriesPagerAdapter(
                            activity = this,
                            items = list,
                            categoryName = categoryName,
                            categoryId = categoryId,
                            subCategoryId = subCategoryId!!
                        )
                        binding.subViewPager.adapter = pagerAdapter
                        binding.subViewPager.offscreenPageLimit = 1

                        TabLayoutMediator(binding.subTabLayout, binding.subViewPager) { tab, position ->
                            val tabView = LayoutInflater.from(this).inflate(R.layout.tab_item2, null)
                            tabView.findViewById<TextView>(R.id.tabTitle).text = list[position].name
                            tab.customView = tabView
                        }.attach()
                    } else {
                        // No 3rd level subcategories, show products directly
                        binding.subTabLayout.isGone = true
                        binding.subViewPager.isGone = true
                        binding.productsContainer.isVisible = true
                        showProducts()
                    }
                }
                is NetworkResults.Error -> {
                    // On error, show products directly
                    binding.subTabLayout.isGone = true
                    binding.subViewPager.isGone = true
                    binding.productsContainer.isVisible = true
                    showProducts()
                }
                else -> Unit
            }
        }
    }

    private fun showProducts() {
        val fragmentTag = if (isFromTrademark) {
            "products_list_trademark_${trademarkId}"
        } else {
            "products_list_${subCategoryId ?: categoryId}"
        }
        
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        
        if (existingFragment == null) {
            val productsFragment = if (isFromTrademark && trademarkId != null) {
                ProductsListFragment.newInstanceForTrademark(
                    trademarkId = trademarkId!!,
                    trademarkName = trademarkName ?: ""
                )
            } else {
                ProductsListFragment.newInstance(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    subCategoryId = subCategoryId ?: categoryId,
                    subSubCategoryId = "all"
                )
            }
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.productsContainer, productsFragment, fragmentTag)
                .commit()
        }
    }

    // Inner adapter class for Activity context
    private class SubSubCategoriesPagerAdapter(
        activity: ProductsActivity,
        private val items: List<SubCategoriesData>,
        private val categoryName: String,
        private val categoryId: String,
        private val subCategoryId: String
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = items.size

        override fun createFragment(position: Int): Fragment {
            val subSubId = items[position].tid.toString()
            return ProductsListFragment.newInstance(
                categoryId = categoryId,
                categoryName = categoryName,
                subCategoryId = subCategoryId,
                subSubCategoryId = subSubId
            )
        }
    }
}