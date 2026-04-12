package com.blueray.marasy.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityTermsBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class TermsActivity : BaseActivity() {

    private lateinit var binding: ActivityTermsBinding
    private val viewmodel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includedTab.title.text = getString(R.string.terms_and_conditions)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }
        viewmodel.retrieveTermsAndConditions()
        getTerms()

    }

    private fun getTerms(){
        viewmodel.getTermsAndConditions().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    binding.title.text = result.data.data.title
                    binding.body.text = result.data.data.body
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