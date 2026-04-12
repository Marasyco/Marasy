package com.blueray.marasy.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blueray.marasy.model.SubCategoriesData
import com.blueray.marasy.ui.fragments.ProductsListFragment

class SubSubCategoriesPagerAdapter(
    fragment: Fragment,
    private val items: List<SubCategoriesData>,           // replace Any with your SubSubCategory model
    private val categoryName: String,
    private val categoryId: String,
    private val subCategoryId: String
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        // Extract tid from items[position]
        val subSubId = try {
            val tidField = items[position]::class.java.getDeclaredField("tid")
            tidField.isAccessible = true
            tidField.get(items[position])?.toString() ?: "all"
        } catch (_: Exception) {
            "all"
        }
        return ProductsListFragment.newInstance(
            categoryId = categoryId,
            categoryName = categoryName,
            subCategoryId = subCategoryId,
            subSubCategoryId = subSubId
        )
    }
}
