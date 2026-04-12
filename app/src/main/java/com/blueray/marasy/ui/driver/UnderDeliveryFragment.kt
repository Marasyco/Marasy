package com.blueray.marasy.ui.driver

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.UnderDeliveryOrdersAdapter
import com.blueray.marasy.databinding.FragmentUnderDeliveryBinding
import com.blueray.marasy.helpers.ViewUtils.hide
import com.blueray.marasy.helpers.ViewUtils.show
import com.blueray.marasy.interfaces.DriverOrdersListener
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel


class UnderDeliveryFragment : Fragment() {

    private lateinit var binding: FragmentUnderDeliveryBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var adapter: UnderDeliveryOrdersAdapter

    companion object {
        private const val ARG_FLAG = "flag"

        // Factory method to create a new instance of this fragment using the provided parameters.
        fun newInstance(flag: Int): UnderDeliveryFragment {
            return UnderDeliveryFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FLAG, flag)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUnderDeliveryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewmodel.retrieveDriverOrders("2")
        getOrders()

        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.retrieveDriverOrders("2")
        }
    }

    private fun getOrders() {
        viewmodel.getDriverOrders().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        binding.swipeRefresh.isRefreshing = false
                        adapter = UnderDeliveryOrdersAdapter(object :
                            DriverOrdersListener {
                            override fun onOrderClick(id: String) {
                                val intent = Intent(
                                    requireContext(),
                                    DriverOrderDetailsActivity::class.java
                                )
                                intent.putExtra("orderId", id)
                                intent.putExtra("orderState", "2")
                                startActivity(intent)
                            }

                            override fun onStartDeliveryClick(
                                id: String,
                                lat: Double,
                                lon: Double
                            ) {
                                TODO("Not yet implemented")
                            }

                        })
                        adapter.submitList(result.data.data)
                        binding.ordersRv.adapter = adapter
                        binding.ordersRv.layoutManager =
                            LinearLayoutManager(
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
                    binding.swipeRefresh.isRefreshing = false
                }

                else -> {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewmodel.retrieveDriverOrders("2")
    }
}