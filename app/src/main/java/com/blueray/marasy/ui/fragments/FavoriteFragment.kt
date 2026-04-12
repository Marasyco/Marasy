package com.blueray.marasy.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.FavoriteAdapter
import com.blueray.marasy.databinding.FragmentFavoriteBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.interfaces.OnFavoriteProductClick
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.activities.ProductDetailsActivity
import com.blueray.marasy.viewmodel.AppViewModel


class FavoriteFragment : Fragment() {


    private lateinit var binding: FragmentFavoriteBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var adapter: FavoriteAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.includedTab.title.text = getString(R.string.favorites)
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
        
        viewmodel.retrieveFavoriteProducts()
        getFavorites()
        getAddToFavorite()
    }


    private fun getFavorites() {
        viewmodel.getFavoriteProducts().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    adapter = FavoriteAdapter(object : OnFavoriteProductClick {
                        override fun onProductDetailsClick(id: String) {
                            val intent =
                                Intent(requireContext(), ProductDetailsActivity::class.java)
                            intent.putExtra("pid", id)
                            startActivity(intent)
                        }

                        override fun onFavoriteClick(id: String, pos: Int) {
                            viewmodel.retrieveAddToFavorite(id)

                        }
                    })
                    adapter.submitList(result.data.data)
                    binding.productsRv.adapter = adapter
                    binding.productsRv.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                }

                is NetworkResults.Error -> {
                    showErrorToast(requireContext(), result.exception.localizedMessage.toString())
                }

                else -> {

                }
            }
        }
    }

    private fun getAddToFavorite() {
        viewmodel.getAddToFavorite().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    Toast.makeText(requireContext(), result.data.msg.message, Toast.LENGTH_SHORT)
                        .show()
                    viewmodel.retrieveFavoriteProducts()
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

}