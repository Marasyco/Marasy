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
import androidx.recyclerview.widget.GridLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.TrademarksGridAdapter
import com.blueray.marasy.databinding.FragmentAllTrademarksBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.interfaces.OnTrademarkClick
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.activities.CartActivity
import com.blueray.marasy.ui.activities.ProductsActivity
import com.blueray.marasy.viewmodel.AppViewModel

class AllTrademarksFragment : Fragment() {

    private lateinit var binding: FragmentAllTrademarksBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var adapter: TrademarksGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllTrademarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.includedTab.title.text = getString(R.string.trademarks)
        binding.includedTab.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Show cart icon and handle navigation
        binding.includedTab.cartButton.visibility = View.VISIBLE
        binding.includedTab.cartButton.setOnClickListener {
            val intent = Intent(requireContext(), CartActivity::class.java)
            startActivity(intent)
        }

        viewmodel.retrieveTrademarks()
        getTrademarks()
    }

    private fun getTrademarks() {
        viewmodel.getTrademarks().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    adapter = TrademarksGridAdapter(object : OnTrademarkClick {
                        override fun onTrademarkClick(pos: Int) {
                            val intent = Intent(requireContext(), ProductsActivity::class.java)
                            intent.putExtra("trademarkName", result.data[pos].name)
                            intent.putExtra("trademarkId", result.data[pos].id)
                            intent.putExtra("isFromTrademark", true)
                            startActivity(intent)
                        }
                    })
                    adapter.submitList(result.data)
                    binding.trademarksRv.adapter = adapter
                    binding.trademarksRv.layoutManager = GridLayoutManager(requireContext(), 3)
                }

                is NetworkResults.Error -> {
                    showErrorToast(requireContext(), result.exception.localizedMessage.toString())
                    Log.d("TrademarksError", result.exception.localizedMessage.toString())
                }

                else -> {

                }
            }
        }
    }
}
