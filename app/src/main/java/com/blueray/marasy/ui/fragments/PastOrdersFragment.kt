package com.blueray.marasy.ui.fragments

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
import com.blueray.marasy.adapters.CurrentOrdersAdapter
import com.blueray.marasy.databinding.FragmentPastOrdersBinding
import com.blueray.marasy.helpers.ViewUtils.hide
import com.blueray.marasy.helpers.ViewUtils.show
import com.blueray.marasy.interfaces.OrderItemListener
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.activities.OrderDetailsActivity
import com.blueray.marasy.viewmodel.AppViewModel

class PastOrdersFragment : Fragment() {
    private lateinit var binding: FragmentPastOrdersBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var adapter: CurrentOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPastOrdersBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewmodel.retrieveViewOrders("2")
        viewOrders()
        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.retrieveViewOrders("2")
        }
    }

    private fun viewOrders() {
        viewmodel.getViewOrders().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        binding.swipeRefresh.isRefreshing = false
                        binding.pastOrdersRv.show()
                        binding.noResultsText.hide()
                        adapter = CurrentOrdersAdapter(result.data.data, object :
                            OrderItemListener {
                            override fun onOrderClick(orderId: String) {
                                val intent =
                                    Intent(requireContext(), OrderDetailsActivity::class.java)
                                intent.putExtra("order_id", orderId)
                                activity?.startActivity(intent)
                            }
                        })
                        binding.pastOrdersRv.adapter = adapter
                        binding.pastOrdersRv.layoutManager =
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                    } else {
                        binding.swipeRefresh.isRefreshing = false
                        binding.pastOrdersRv.hide()
                        binding.noResultsText.show()

                    }
                }

                is NetworkResults.Error -> {
                    binding.swipeRefresh.isRefreshing = false
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
}