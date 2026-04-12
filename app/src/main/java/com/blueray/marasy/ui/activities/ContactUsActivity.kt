package com.blueray.marasy.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityContactUsBinding
import com.blueray.marasy.viewmodel.AppViewModel

class ContactUsActivity : Fragment() {
    private lateinit var binding: ActivityContactUsBinding
    private val viewModel by viewModels<AppViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.includedTab.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.includedTab.title.text = getString(R.string.support)
        
        // Phone number from the layout (0789105999)
        val phoneNumber = "0798208089"
        
        // Phone icon click - open dialer
        binding.circleImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        }
        
        // WhatsApp icon click - open WhatsApp
        binding.whats.setOnClickListener {
            openWhatsApp(phoneNumber)
        }
    }
    
    private fun openWhatsApp(phoneNumber: String) {
        // Convert phone number to international format (remove leading 0, add country code 962 for Jordan)
        val internationalNumber = if (phoneNumber.startsWith("0")) {
            "962${phoneNumber.substring(1)}"
        } else {
            phoneNumber
        }
        
        try {
            val uri = Uri.parse("https://wa.me/$internationalNumber")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.whatsapp")
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web WhatsApp if app not installed
            try {
                val uri = Uri.parse("https://wa.me/$internationalNumber")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(requireContext(), getString(R.string.whatsapp_not_installed), Toast.LENGTH_SHORT).show()
            }
        }
    }
}