package com.blueray.marasy.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.adapters.ProductsAdapter
import com.blueray.marasy.databinding.FragmentSearchBinding
import com.blueray.marasy.interfaces.ProductListener
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.model.Product
import com.blueray.marasy.ui.activities.ProductDetailsActivity
import com.blueray.marasy.viewmodel.AppViewModel


class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var adapter: ProductsAdapter

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private var currentQuery: String = ""
    private var pageSize = 1
    private val allProducts = mutableListOf<Product>() // Adjust type based on your model
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductsAdapter(object : ProductListener {
            override fun onProductClick(product: Product) {
                val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
                intent.putExtra("pid", product.pid)
                intent.putExtra("categoryName", "")
                startActivity(intent)
            }

            override fun onFavoriteToggle(product: Product, isChecked: Boolean) {
                if (com.blueray.marasy.helpers.HelperUtils.isGuest(requireContext())) {
                    com.blueray.marasy.helpers.HelperUtils.showLoginRequiredDialog(requireActivity()) {
                        val intent = Intent(requireContext(), com.blueray.marasy.ui.activities.LoginActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    viewmodel.retrieveAddToFavorite(product.pid)
                }
            }


        })
        binding.searchRv.adapter = adapter
        binding.searchRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL , false)


        binding.includedTab.title.text = getString(R.string.search)

        binding.includedTab.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


        // Scroll listener for pagination
        binding.searchRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadNextPage()
                    }
                }
            }
        })

        // Search text change
        binding.searchEt.addTextChangedListener { text ->
            if (text?.length ?: 0 >= 3) {
                currentQuery = text.toString()
                currentPage = 0
                isLastPage = false
                allProducts.clear()
                adapter.submitList(emptyList())
                fetchSearchResults()
            } else {
                currentQuery = ""
                allProducts.clear()
                adapter.submitList(emptyList())
                binding.searchRv.visibility = View.GONE
            }
        }


        // Search button
        binding.searchImage.setOnClickListener {
            if (binding.searchEt.text.isNotEmpty()) {
                currentQuery = binding.searchEt.text.toString()
                currentPage = 0
                isLastPage = false
                fetchSearchResults()
            }
        }

        // Search on keyboard action
        binding.searchEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH && binding.searchEt.text.isNotEmpty()) {
                currentQuery = binding.searchEt.text.toString()
                currentPage = 0
                isLastPage = false
                fetchSearchResults()
                true
            } else false
        }

        observeSearchResults()
        observeAddToFavorite()
    }

    private fun loadNextPage() {
        currentPage++
        fetchSearchResults()
    }

    private fun fetchSearchResults() {
        isLoading = true
//        toggleLoading(false)
        viewmodel.retrieveSearch(
            currentQuery,
            currentPage.toString(),
            "0"
        )
    }

    private fun observeSearchResults() {
        viewmodel.getSearch().observe(viewLifecycleOwner) { result ->
            isLoading = false
//            toggleLoading(false)

            when (result) {
                is NetworkResults.Success -> {
                    val products = result.data.data ?: emptyList()
                    pageSize = result.data.pager.limit

                    if (currentPage == 0) {
                        allProducts.clear() // Clear only for first page
                    }

                    allProducts.addAll(products)
                    adapter.submitList(allProducts.toList()) // Submit a copy to avoid diffing issues

                    binding.searchRv.visibility = if (allProducts.isEmpty()) View.GONE else View.VISIBLE

                    if (products.size < pageSize) {
                        isLastPage = true
                    }
                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.exception.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }

        }
    }

    private fun observeAddToFavorite() {
        viewmodel.getAddToFavorite().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        Toast.makeText(
                            requireContext(),
                            result.data.msg.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.exception.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }
}