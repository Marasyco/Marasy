package com.blueray.marasy.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.adapters.ProductsAdapter
import com.blueray.marasy.databinding.FragmentProductsListBinding
import com.blueray.marasy.helpers.RecyclerItemClickListener
import com.blueray.marasy.interfaces.ProductListener
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.model.Product
import com.blueray.marasy.ui.activities.ProductDetailsActivity
import com.blueray.marasy.viewmodel.AppViewModel

class ProductsListFragment : Fragment() {

    private var _binding: FragmentProductsListBinding? = null
    private val binding get() = _binding!!

    private val viewmodel by viewModels<AppViewModel>()

    private lateinit var categoryName: String
    private lateinit var categoryId: String
    private lateinit var subCategoryId: String
    private lateinit var subSubCategoryId: String // "all" means show all under subCategory
    
    // Trademark support
    private var isFromTrademark: Boolean = false
    private var trademarkId: String? = null
    private var trademarkName: String? = null

    private lateinit var adapter: ProductsAdapter

    // pagination
    private var page = 1
    private val pageSize = 20
    private var isLoading = false
    private var isLastPage = false

    companion object {
        fun newInstance(
            categoryId: String,
            categoryName: String,
            subCategoryId: String,
            subSubCategoryId: String
        ): ProductsListFragment {
            return ProductsListFragment().apply {
                arguments = Bundle().apply {
                    putString("categoryId", categoryId)
                    putString("categoryName", categoryName)
                    putString("subCategoryId", subCategoryId)
                    putString("subSubCategoryId", subSubCategoryId)
                    putBoolean("isFromTrademark", false)
                }
            }
        }
        
        fun newInstanceForTrademark(
            trademarkId: String,
            trademarkName: String
        ): ProductsListFragment {
            return ProductsListFragment().apply {
                arguments = Bundle().apply {
                    putString("trademarkId", trademarkId)
                    putString("trademarkName", trademarkName)
                    putBoolean("isFromTrademark", true)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        isFromTrademark = requireArguments().getBoolean("isFromTrademark", false)
        
        if (isFromTrademark) {
            trademarkId = requireArguments().getString("trademarkId")
            trademarkName = requireArguments().getString("trademarkName")
        } else {
            categoryId = requireArguments().getString("categoryId").orEmpty()
            categoryName = requireArguments().getString("categoryName").orEmpty()
            subCategoryId = requireArguments().getString("subCategoryId").orEmpty()
            subSubCategoryId = requireArguments().getString("subSubCategoryId").orEmpty()
        }

        setupRecycler()
        setupSwipe()

        // initial load
        loadPage(reset = true)

        observeProducts()
        observeFavorite()
    }

    private fun setupRecycler() {
        adapter = ProductsAdapter(
            object : ProductListener {
                override fun onProductClick(product: Product) {
                    val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
                    intent.putExtra("pid", product.pid)
                    startActivity(intent)
                    Log.d("POPOPO", "wewewe")
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
            }
        )
        val glm = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.productsRv.layoutManager = glm
        binding.productsRv.adapter = adapter

        // Add scroll listener for pagination
        binding.productsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val visible = glm.childCount
                val total = glm.itemCount
                val firstVisible = glm.findFirstVisibleItemPosition()

                val shouldLoadMore = !isLoading && !isLastPage &&
                        (visible + firstVisible) >= (total - 4) // near the end
                if (shouldLoadMore) {
                    loadPage(reset = false)
                }
            }
        })
    }

    private fun setupSwipe() {
        binding.swipe.setOnRefreshListener {
            loadPage(reset = true)
        }
    }

    private fun loadPage(reset: Boolean) {
        if (reset) {
            page = 0
            isLastPage = false
            adapter.submitList(emptyList())
            binding.emptyView.isVisible = false
        }
        if (isLastPage || isLoading) return

        isLoading = true
        if (!binding.swipe.isRefreshing) binding.progress.isVisible = true

        if (isFromTrademark && trademarkId != null) {
            // Load products by trademark
            viewmodel.retrieveTrademarkProducts(trademarkId!!, page.toString())
        } else {
            // Load products by category
            if (subSubCategoryId == "all") {
                viewmodel.retrieveCategoryProducts(categoryId, subCategoryId, page.toString())
            } else {
                viewmodel.retrieveCategoryProducts(subCategoryId, subSubCategoryId, page.toString())
            }
        }
    }

    private fun observeProducts() {
        // Observe the appropriate LiveData based on source
        val productsLiveData = if (isFromTrademark) {
            viewmodel.getTrademarkProducts()
        } else {
            viewmodel.getCategoryProducts()
        }
        
        productsLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    binding.progress.isVisible = false
                    binding.swipe.isRefreshing = false
                    isLoading = false

                    val items = result.data.data // expect List<ProductLike>
                    // Determine last page
                    if (items.size < pageSize) isLastPage = true else page++

                    val newList = adapter.current + items
                    adapter.submitList(newList)
                    binding.emptyView.isVisible = newList.isEmpty()
                }

                is NetworkResults.Error -> {
                    binding.progress.isVisible = false
                    binding.swipe.isRefreshing = false
                    isLoading = false
                    // Optionally show a toast/snackbar
                }

                else -> Unit
            }
        }
    }

    private fun observeFavorite() {
        viewmodel.getAddToFavorite().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    Toast.makeText(requireContext(), result.data.msg.message, Toast.LENGTH_SHORT)
                        .show()
                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.exception.localizedMessage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
