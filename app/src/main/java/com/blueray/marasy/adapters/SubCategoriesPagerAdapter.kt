package com.blueray.marasy.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blueray.marasy.model.SubCategoriesData
import com.blueray.marasy.ui.fragments.ProductsFragment

class SubCategoriesPagerAdapter(
    activity: FragmentActivity,
    private val subCategories: List<SubCategoriesData>,
    private val categoryName: String,
    private val categoryId: String
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = subCategories.size

    override fun createFragment(position: Int): Fragment {
        return ProductsFragment.newInstance(
            subCategories[position].tid.toString(),
            categoryName,
            categoryId,
            hasChilds = subCategories[position].has_childs
        )
    }
}