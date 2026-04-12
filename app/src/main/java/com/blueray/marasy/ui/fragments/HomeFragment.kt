package com.blueray.marasy.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.BestSellingAdapter
import com.blueray.marasy.adapters.HomeCategoriesAdapter
import com.blueray.marasy.adapters.HomeOffersAdapter
import com.blueray.marasy.adapters.TrademarksAdapter
import com.blueray.marasy.databinding.FragmentHomeBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.interfaces.HomeOffersListener
import com.blueray.marasy.interfaces.OnBestSellingClick
import com.blueray.marasy.interfaces.OnCategoryClick
import com.blueray.marasy.interfaces.OnTrademarkClick
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.activities.CartActivity
import com.blueray.marasy.ui.activities.HomeActivity
import com.blueray.marasy.ui.activities.ProductDetailsActivity
import com.blueray.marasy.ui.activities.ProductsActivity
import com.blueray.marasy.ui.activities.SubCategoriesActivity
import com.blueray.marasy.viewmodel.AppViewModel
import com.denzcoskun.imageslider.constants.AnimationTypes
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel


class HomeFragment : Fragment() {

    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var categoriesAdapter: HomeCategoriesAdapter
    private lateinit var trademarksAdapter: TrademarksAdapter
    private lateinit var bestSellingAdapter: BestSellingAdapter
    private lateinit var offersAdapter: HomeOffersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.includedTab.menuButton.setOnClickListener {
            (requireActivity() as HomeActivity).openDrawer()
        }
        binding.includedTab.cartButton.setOnClickListener {
            if (com.blueray.marasy.helpers.HelperUtils.isGuest(requireContext())) {
                com.blueray.marasy.helpers.HelperUtils.showLoginRequiredDialog(requireActivity()) {
                    val intent = Intent(requireContext(), com.blueray.marasy.ui.activities.LoginActivity::class.java)
                    startActivity(intent)
                }
            } else {
                val intent = Intent(requireContext(), CartActivity::class.java)
                startActivity(intent)
            }
        }

        binding.swipe.setOnRefreshListener {
            binding.swipe.isRefreshing = false
        }

        binding.viewCategoriesTv.setOnClickListener {
            findNavController().navigate(R.id.allCategoriesFragment)
        }

        binding.viewTrademarksTv.setOnClickListener {
            findNavController().navigate(R.id.allTrademarksFragment)
        }

        binding.searchCard.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }


        viewmodel.retrieveHomeSlider()
        viewmodel.retrieveMainCategories()
        viewmodel.retrieveTrademarks()
        viewmodel.retrieveCart()
        viewmodel.retrieveBestSelling()
        viewmodel.retrieveOfferProducts()


        getHomeSlider()
        getCategories()
        getTrademarks()
        getCart()
        getBestSelling()
        getOffers()
    }

    private fun getHomeSlider() {
        viewmodel.getHomeSlider().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        val imageList = ArrayList<SlideModel>()

                        result.data.data.forEach { url ->
                            imageList.add(
                                SlideModel(
                                    HelperUtils.BASE_URL + url.images,
                                    ScaleTypes.FIT
                                )
                            )
                        }


                        binding.imageSlider.setImageList(imageList)
                        binding.imageSlider.setSlideAnimation(AnimationTypes.ZOOM_IN)
                    }
                }

                is NetworkResults.Error -> {

                }

                else -> {

                }
            }

        }
    }

    private fun getCategories() {
        viewmodel.getMainCategories().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        categoriesAdapter = HomeCategoriesAdapter(object : OnCategoryClick {
                            override fun onCategoryItemClick(pos: Int) {
                                val intent = Intent(requireContext(), SubCategoriesActivity::class.java)
                                intent.putExtra("categoryName", result.data.data[pos].name)
                                intent.putExtra("categoryId", result.data.data[pos].id)
                                startActivity(intent)
                            }
                        })
                        categoriesAdapter.submitList(result.data.data)
                        binding.categoriesRv.adapter = categoriesAdapter
                        binding.categoriesRv.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    }
                }

                is NetworkResults.Error -> {

                }

                else -> {

                }

            }
        }
    }

    private fun getTrademarks() {
        viewmodel.getTrademarks().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    // Direct array response, no msg wrapper
                    trademarksAdapter = TrademarksAdapter(object : OnTrademarkClick {
                        override fun onTrademarkClick(pos: Int) {
                            val intent = Intent(requireContext(), ProductsActivity::class.java)
                            intent.putExtra("trademarkName", result.data[pos].name)
                            intent.putExtra("trademarkId", result.data[pos].id)
                            intent.putExtra("isFromTrademark", true)
                            startActivity(intent)
                        }
                    })
                    trademarksAdapter.submitList(result.data)
                    binding.trademarksRv.adapter = trademarksAdapter
                    binding.trademarksRv.layoutManager = LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }

                is NetworkResults.Error -> {
                    Log.d("TrademarksError", result.exception.localizedMessage.toString())
                }

                else -> {

                }
            }
        }
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

    private fun getBestSelling() {
        viewmodel.getBestSelling().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        bestSellingAdapter = BestSellingAdapter(object : OnBestSellingClick {
                            override fun onProductDetailsClick(id: String) {
                                val intent =
                                    Intent(requireContext(), ProductDetailsActivity::class.java)
                                intent.putExtra("pid", id)
                                startActivity(intent)

                            }
                        })
                        bestSellingAdapter.submitList(result.data.data)
                        binding.bestSellingRv.adapter = bestSellingAdapter
                        binding.bestSellingRv.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )


                    }
                }

                is NetworkResults.Error -> {
                    Log.d("werewqe", result.exception.localizedMessage.toString())
                }

                else -> {
                    Log.d("werewqe", result.toString())
                }
            }
        }
    }

    private fun getOffers() {
        viewmodel.getOfferProducts().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        offersAdapter = HomeOffersAdapter(object : HomeOffersListener {
                            override fun onProductClick(pid: String) {
                                val intent =
                                    Intent(requireContext(), ProductDetailsActivity::class.java)
                                intent.putExtra("pid", pid)
                                startActivity(intent)
                            }
                        })
                        offersAdapter.submitList(result.data.data)
                        binding.offersRv.adapter = offersAdapter
                        binding.offersRv.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    }

                }

                is NetworkResults.Error -> {
                    Log.d("OffersError", result.exception.localizedMessage.toString())
                }

                else -> {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewmodel.retrieveCart()

    }
}