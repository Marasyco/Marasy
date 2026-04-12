package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.blueray.marasy.adapters.SubCategoriesAdapter
import com.blueray.marasy.databinding.ActivitySubCategoriesBinding
import com.blueray.marasy.helpers.ViewUtils.show
import com.blueray.marasy.interfaces.OnCategoryClick
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class SubCategoriesActivity : BaseActivity() {
    private lateinit var binding: ActivitySubCategoriesBinding
    private val viewmodel by viewModels<AppViewModel>()
    private var categoryName = ""
    private var categoryId = ""
    private lateinit var adapter: SubCategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryName = intent.getStringExtra("categoryName").toString()
        categoryId = intent.getStringExtra("categoryId").toString()

        binding.includedTab.backButton.setOnClickListener {
            finish()
        }
        binding.includedTab.title.text = categoryName
        binding.includedTab.backButton.show()

        // Show cart icon and handle navigation
        binding.includedTab.cartButton.visibility = android.view.View.VISIBLE
        binding.includedTab.cartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        viewmodel.retrieveSubCategories(categoryId)
        getSubCategories()

    }

    private fun getSubCategories() {
        viewmodel.getSubCategories().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        // Check if there are subcategories
                        if (result.data.data.isEmpty()) {
                            // No subcategories, navigate directly to products
                            val intent = Intent(this, ProductsActivity::class.java)
                            intent.putExtra("categoryName", categoryName)
                            intent.putExtra("categoryId", categoryId)
                            startActivity(intent)
                            finish()
                        } else {
                            // Show subcategories
                            adapter = SubCategoriesAdapter(object : OnCategoryClick {
                                override fun onCategoryItemClick(pos: Int) {
                                    val subCategory = result.data.data[pos]
                                    val intent = Intent(this@SubCategoriesActivity, ProductsActivity::class.java)
                                    intent.putExtra("categoryName", subCategory.name)
                                    intent.putExtra("categoryId", categoryId)
                                    intent.putExtra("subCategoryId", subCategory.tid.toString())
                                    intent.putExtra("hasChild", subCategory.has_childs)
                                    startActivity(intent)
                                }
                            })

                            binding.subCategoriesRv.adapter = adapter
                            adapter.submitList(result.data.data)
                            binding.subCategoriesRv.layoutManager = GridLayoutManager(this, 3)
                        }
                    }
                }

                is NetworkResults.ErrorMessage -> {
                    // If error, try to show products directly
                    val intent = Intent(this, ProductsActivity::class.java)
                    intent.putExtra("categoryName", categoryName)
                    intent.putExtra("categoryId", categoryId)
                    startActivity(intent)
                    finish()
                }

                is NetworkResults.Error -> {
                    // If error, try to show products directly
                    val intent = Intent(this, ProductsActivity::class.java)
                    intent.putExtra("categoryName", categoryName)
                    intent.putExtra("categoryId", categoryId)
                    startActivity(intent)
                    finish()
                }

                else -> {}
            }
        }
    }
}