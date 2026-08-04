package com.blueray.marasy.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.CategoriesAdapter
import com.blueray.marasy.adapters.HomeCategoriesAdapter
import com.blueray.marasy.databinding.FragmentAllCategoriesBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.interfaces.OnCategoryClick
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.activities.SubCategoriesActivity
import com.blueray.marasy.viewmodel.AppViewModel


class AllCategoriesFragment : Fragment() {

    private lateinit var binding: FragmentAllCategoriesBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var adapter: CategoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.includedTab.title.text = getString(R.string.categories)
        binding.includedTab.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Show cart icon with badge and handle navigation
        binding.includedTab.cartButton.visibility = View.VISIBLE
        binding.includedTab.cartCountTv.visibility = View.VISIBLE
        binding.includedTab.cartButton.setOnClickListener {
            val intent = Intent(requireContext(), com.blueray.marasy.ui.activities.CartActivity::class.java)
            startActivity(intent)
        }

        viewmodel.retrieveMainCategories()
        viewmodel.retrieveCart()
        getCategories()
        getCart()
    }

    private fun getCart() {
        viewmodel.getViewCart().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        binding.includedTab.cartCountTv.text =
                            result.data.data.items.count().toString()
                    } else {
                        binding.includedTab.cartCountTv.text = "0"
                    }
                }
                is NetworkResults.Error -> {
                    binding.includedTab.cartCountTv.text = "0"
                }
                else -> {
                    binding.includedTab.cartCountTv.text = "0"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewmodel.retrieveCart()
    }

    private fun getCategories() {
        viewmodel.getMainCategories().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        adapter = CategoriesAdapter(object : OnCategoryClick {
                            override fun onCategoryItemClick(pos: Int) {
                                val intent = Intent(requireContext(), SubCategoriesActivity::class.java)
                                intent.putExtra("categoryName", result.data.data[pos].name)
                                intent.putExtra("categoryId", result.data.data[pos].id)
                                startActivity(intent)
                            }
                        })
                        adapter.submitList(result.data.data)
                        binding.categoriesRv.adapter = adapter
                        binding.categoriesRv.layoutManager = GridLayoutManager(requireContext(), 3)
                    }
                }

                is NetworkResults.Error -> {
                    showErrorToast(requireContext(), result.exception.localizedMessage.toString())
                }

                else -> {

                }
            }
        }
    }


}