package com.blueray.marasy.ui.driver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.DriverOrdersAdapter
import com.blueray.marasy.databinding.FragmentDriverOrdersBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.helpers.HelperUtils.openGoogleMaps
import com.blueray.marasy.helpers.HelperUtils.showConfirmDialog
import com.blueray.marasy.helpers.ViewUtils.hide
import com.blueray.marasy.helpers.ViewUtils.show
import com.blueray.marasy.interfaces.DriverOrdersListener
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class DriverOrdersFragment : Fragment() {

    private lateinit var binding: FragmentDriverOrdersBinding
    private val viewmodel by viewModels<AppViewModel>()
    private var lat = 0.00
    private var lon = 0.00
    companion object {
        private const val ARG_FLAG = "flag"

        // Factory method to create a new instance of this fragment using the provided parameters.
        fun newInstance(flag: Int): DriverOrdersFragment {
            return DriverOrdersFragment().apply {
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
        binding = FragmentDriverOrdersBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewmodel.retrieveDriverOrders("1")
        getOrders()
        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.retrieveDriverOrders("1")
        }
    }

    private fun getOrders() {
        viewmodel.getDriverOrders().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        binding.swipeRefresh.isRefreshing = false
                        val adapter =
                            DriverOrdersAdapter(result.data.data, object : DriverOrdersListener {
                                override fun onStartDeliveryClick(
                                    id: String,
                                    lat: Double,
                                    lon: Double
                                ) {
                                    this@DriverOrdersFragment.lat = lat
                                    this@DriverOrdersFragment.lon = lon
                                    showConfirmDialog(
                                        requireActivity(),
                                        getString(R.string.start_delivering_order)
                                    ) {
                                        viewmodel.retrieveStartEndEmployeeOrder(
                                            id,
                                            "1", // 1 for start
                                            "2" // 2 for driver
                                        )
                                        getStartEndOrder()
                                    }
                                }

                                override fun onOrderClick(id: String) {
                                    val intent = Intent(
                                        requireContext(),
                                        DriverOrderDetailsActivity::class.java
                                    )
                                    intent.putExtra("orderId", id)
                                    intent.putExtra("orderState", "1")
                                    startActivity(intent)
                                }

                            })
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
    private fun getStartEndOrder() {
        viewmodel.getStartEndEmployeeOrder().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    Toast.makeText(requireContext(), result.data.msg.message, Toast.LENGTH_SHORT)
                        .show()
                    openInGoogleMaps(requireContext(), lat, lon)
                    viewmodel.retrieveDriverOrders("1")
                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.exception.localizedMessage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {

                }
            }
        }
    }

    private fun openInGoogleMaps(context: Context, latitude: Double, longitude: Double) {
        HelperUtils.openGoogleMaps(context, latitude, longitude)
    }

    override fun onResume() {
        super.onResume()
        viewmodel.retrieveDriverOrders("1")
    }
}