package com.blueray.marasy.ui.shopper

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.ShopperOrdersAdapter
import com.blueray.marasy.databinding.FragmentShopperOrdersBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.helpers.ViewUtils.hide
import com.blueray.marasy.helpers.ViewUtils.show
import com.blueray.marasy.interfaces.ShopperOrdersListener
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class ShopperOrdersFragment : Fragment() {
    private lateinit var binding: FragmentShopperOrdersBinding
    private val viewmodel by viewModels<AppViewModel>()
    private var flag: Int = 1
    private lateinit var adapter: ShopperOrdersAdapter

    companion object {
        private const val ARG_FLAG = "flag"

        fun newInstance(flag: Int): ShopperOrdersFragment {
            return ShopperOrdersFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FLAG, flag)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            flag = it.getInt(ARG_FLAG, 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShopperOrdersBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewmodel.retrieveShopperOrders(flag.toString())
        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.retrieveShopperOrders(flag.toString())
        }

        getOrders()
    }

    private fun getOrders() {
        viewmodel.getShopperOrders().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        binding.swipeRefresh.isRefreshing = false
                        adapter = ShopperOrdersAdapter(object : ShopperOrdersListener {
                            override fun onOrderClick(id: String, pos: Int) {
                                val intent = Intent(
                                    requireContext(),
                                    ShopperOrderDetailsActivity::class.java
                                )
                                intent.putExtra("orderId", id)
                                intent.putExtra(
                                    "paymentWay",
                                    result.data.data[pos].payment_gateway
                                )
                                intent.putExtra(
                                    "flag", flag.toString()
                                )
                                intent.putExtra(
                                    "numberOfItems",
                                    result.data.data[pos].order_items_count.toString()
                                )
                                intent.putExtra(
                                    "price",
                                    result.data.data[pos].total_order_price.toString()
                                )
                                intent.putExtra("notes", result.data.data[pos].notes)
                                startActivity(intent)
                            }
                        })
                        adapter.submitList(result.data.data)
                        binding.ordersRv.adapter = adapter
                        binding.ordersRv.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        binding.ordersRv.show()
                        binding.noOrdersText.hide()
                    } else {
                        binding.swipeRefresh.isRefreshing = false
                        binding.ordersRv.hide()
                        binding.noOrdersText.show()
                    }
                }

                is NetworkResults.Error -> {
                    showErrorToast(requireContext(), result.exception.localizedMessage.toString())
                    binding.swipeRefresh.isRefreshing = false
                }

                else -> {

                }
            }
        }
    }
}
