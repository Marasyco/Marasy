package com.blueray.marasy.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.WarrantiesAdapter
import com.blueray.marasy.databinding.ActivityWarrantiesBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class WarrantiesActivity : BaseActivity() {

    private lateinit var binding: ActivityWarrantiesBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var adapter: WarrantiesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWarrantiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includedTab.title.text = getString(R.string.warranties)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }

        viewmodel.retrieveWarranties()
        getWarranties()
    }

    private fun getWarranties() {
        viewmodel.getWarranties().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        adapter = WarrantiesAdapter()
                        adapter.submitList(result.data.data)
                        binding.warrantiesRv.adapter = adapter
                        binding.warrantiesRv.layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    }
                }

                is NetworkResults.Error -> {
                    showErrorToast(this, result.exception.localizedMessage.toString())
                }

                else -> {

                }
            }
        }
    }
}