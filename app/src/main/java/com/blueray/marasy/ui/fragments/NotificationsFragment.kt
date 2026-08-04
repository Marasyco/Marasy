package com.blueray.marasy.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.NotificationsAdapter
import com.blueray.marasy.databinding.FragmentNotificationsBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.interfaces.NotificationListener
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.activities.ProductDetailsActivity
import com.blueray.marasy.viewmodel.AppViewModel


class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var adapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        (activity as HomeActivity).hideBottomNav()
        binding.includedTab.title.text = getString(R.string.notifications)
        binding.includedTab.backButton.visibility = View.GONE
        binding.includedTab.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        
        if (com.blueray.marasy.helpers.HelperUtils.isGuest(requireContext())) {
            com.blueray.marasy.helpers.HelperUtils.showLoginRequiredDialog(requireActivity()) {
                val intent = Intent(requireContext(), com.blueray.marasy.ui.activities.LoginActivity::class.java)
                startActivity(intent)
            }
            return
        }
        
        binding.notificationsRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.swipeRefresh.setColorSchemeResources(R.color.orange)
        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.retrieveMyNotifications()
        }

        viewmodel.retrieveMyNotifications()
        getNotifications()
    }

    private fun getNotifications() {
        viewmodel.getMyNotifications().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    binding.swipeRefresh.isRefreshing = false
                    adapter = NotificationsAdapter(object : NotificationListener {
                        override fun onNotificationClick(id: String, type: String) {
                            if (type == "product") {
                                val intent =
                                    Intent(requireContext(), ProductDetailsActivity::class.java)
                                intent.putExtra("pid", id)
                                startActivity(intent)
                            } else if (type == "category") {
//                                val intent =
//                                    Intent(requireContext(), ProductsActivity::class.java)
//                                intent.putExtra("categoryId", id)
//                                startActivity(intent)
                            } else if (type == "brand") {
//                                val bundle = Bundle().apply {
//                                    putString("id", id)
//                                }
//                                findNavController().navigate(R.id.brandProductsFragment, bundle)
                            }
                        }
                    })
                    adapter.submitList(result.data.data_1)
                    binding.notificationsRv.adapter = adapter
                }

                is NetworkResults.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    showErrorToast(requireContext(), result.exception.localizedMessage.toString())
                }

                else -> {}
            }
        }
    }
}