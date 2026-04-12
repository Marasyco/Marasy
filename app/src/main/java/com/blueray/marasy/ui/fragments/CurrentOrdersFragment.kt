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
import com.blueray.marasy.databinding.FragmentCurrentOrdersBinding
import com.blueray.marasy.helpers.ViewUtils.hide
import com.blueray.marasy.helpers.ViewUtils.show
import com.blueray.marasy.interfaces.OrderItemListener
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.activities.OrderDetailsActivity
import com.blueray.marasy.viewmodel.AppViewModel


class CurrentOrdersFragment : Fragment() {
    private lateinit var binding: FragmentCurrentOrdersBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var adapter: CurrentOrdersAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCurrentOrdersBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewmodel.retrieveViewOrders("1")
        viewOrders()

        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.retrieveViewOrders("1")
        }
    }

    private fun viewOrders() {
        viewmodel.getViewOrders().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        binding.swipeRefresh.isRefreshing = false
                        binding.currentOrdersRv.show()
                        binding.noResultsText.hide()
                        adapter =
                            CurrentOrdersAdapter(result.data.data, object : OrderItemListener {
                                override fun onOrderClick(orderId: String) {
                                    val intent =
                                        Intent(requireContext(), OrderDetailsActivity::class.java)
                                    intent.putExtra("order_id", orderId)
                                    activity?.startActivity(intent)
                                }
                            })
                        binding.currentOrdersRv.adapter = adapter
                        binding.currentOrdersRv.layoutManager =
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                    } else {
                        binding.swipeRefresh.isRefreshing = false
                        binding.currentOrdersRv.hide()
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