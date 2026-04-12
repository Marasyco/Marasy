package com.blueray.marasy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.blueray.marasy.R
import com.blueray.marasy.adapters.SubSubCategoriesPagerAdapter
import com.blueray.marasy.databinding.FragmentProductsBinding
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class ProductsFragment : Fragment() {

    private lateinit var binding: FragmentProductsBinding
    private val viewmodel by viewModels<AppViewModel>()

    private lateinit var categoryName: String
    private lateinit var categoryId: String
    private lateinit var subCategoryId: String
    private var hasChilds: Boolean = false

    private lateinit var pagerAdapter: SubSubCategoriesPagerAdapter

    companion object {
        fun newInstance(
            tid: String,
            categoryName: String,
            categoryId: String,
            hasChilds: Boolean
        ): ProductsFragment {
            return ProductsFragment().apply {
                arguments = Bundle().apply {
                    putString("subCategoryId", tid)
                    putString("categoryName", categoryName)
                    putString("categoryId", categoryId)
                    putBoolean("hasChilds", hasChilds)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryName = requireArguments().getString("categoryName").orEmpty()
        categoryId = requireArguments().getString("categoryId").orEmpty()
        subCategoryId = requireArguments().getString("subCategoryId").orEmpty()
        hasChilds = requireArguments().getBoolean("hasChilds", false)

        if (hasChilds) {
            binding.subTabLayout.isVisible = true
            binding.subViewPager.isVisible = true
            binding.listContainer.isGone = true

            viewmodel.retrieveSubCategories(subCategoryId)
            observeSubSubCategories()
        } else {
            binding.subTabLayout.isGone = true
            binding.subViewPager.isGone = true
            binding.listContainer.isVisible = true

            val tag = "list_$subCategoryId"
            val existing = childFragmentManager.findFragmentByTag(tag)
            if (existing == null) {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.listContainer,
                        ProductsListFragment.newInstance(
                            categoryId = categoryId,
                            categoryName = categoryName,
                            subCategoryId = subCategoryId,
                            subSubCategoryId = "all" // instruct the child to fetch whole subCategory
                        ),
                        tag
                    )
                    .commit()
            }
        }
    }

    private fun observeSubSubCategories() {
        viewmodel.getSubCategories().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    val list = result.data.data
                    pagerAdapter = SubSubCategoriesPagerAdapter(
                        fragment = this,
                        items = list,
                        categoryName = categoryName,
                        categoryId = categoryId,
                        subCategoryId = subCategoryId,
                    )
                    binding.subViewPager.adapter = pagerAdapter
                    binding.subViewPager.offscreenPageLimit = 1

                    TabLayoutMediator(binding.subTabLayout, binding.subViewPager) { tab, position ->
                        val tabView = layoutInflater.inflate(R.layout.tab_item2, null)
                        tabView.findViewById<TextView>(R.id.tabTitle).text = list[position].name
                        tab.customView = tabView
                    }.attach()
                }
                is NetworkResults.Error -> {
                    binding.subTabLayout.isGone = true
                    binding.subViewPager.isGone = true
                    binding.listContainer.isVisible = true

                    childFragmentManager.beginTransaction()
                        .replace(
                            R.id.listContainer,
                            ProductsListFragment.newInstance(
                                categoryId = categoryId,
                                categoryName = categoryName,
                                subCategoryId = subCategoryId,
                                subSubCategoryId = "all"
                            )
                        )
                        .commit()
                }
                else -> Unit
            }
        }
    }
}
