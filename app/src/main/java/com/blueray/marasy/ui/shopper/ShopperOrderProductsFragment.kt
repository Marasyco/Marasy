package com.blueray.marasy.ui.shopper

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.ShopperOrderProductsAdapter
import com.blueray.marasy.databinding.FragmentShopperOrderProductsBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.helpers.HelperUtils.showToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class ShopperOrderProductsFragment : Fragment() {
    private lateinit var binding: FragmentShopperOrderProductsBinding
    private val viewmodel by viewModels<AppViewModel>()

    private var orderId: String? = null
    private var category: String? = null
    private var flag: Int = 0

    private var scannedItemTitle: String? = null
    private var scannedItemId: String? = null

    companion object {
        private const val ARG_FLAG = "flag"
        private const val ARG_ORDER_ID = "orderId"
        private const val ARG_CATEGORY = "category"

        fun newInstance(
            flag: Int,
            orderId: String,
            category: String
        ): ShopperOrderProductsFragment {
            return ShopperOrderProductsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FLAG, flag)
                    putString(ARG_ORDER_ID, orderId)
                    putString(ARG_CATEGORY, category)
                }
            }
        }
    }

    private val scanLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intentResult: IntentResult? =
                IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            if (intentResult != null) {
                if (intentResult.contents != null) {
                    // ✅ Scanned successfully
                    Log.d("SCan result", intentResult.contents.toString())
                    processBarcode(intentResult.contents.toString())
                } else {
                    // Scan canceled or failed - show manual entry dialog
                    showManualBarcodeDialog()
                }
            } else {
                // Scan failed - show manual entry dialog
                showManualBarcodeDialog()
            }
        }
    
    private fun processBarcode(barcode: String) {
        viewmodel.retrieveSetShopperOrder(
            orderId.toString(),
            scannedItemId.toString(),
            "1",
            barcode
        )
    }
    
    private fun showManualBarcodeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_manual_barcode, null)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Set dialog width to match parent with margins
        val displayMetrics = resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 0.9).toInt() // 90% of screen width
        dialog.window?.setLayout(dialogWidth, android.view.WindowManager.LayoutParams.WRAP_CONTENT)
        
        val barcodeEditText = dialogView.findViewById<TextInputEditText>(R.id.barcodeEditText)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<MaterialButton>(R.id.btnConfirm)
        val dialogMessage = dialogView.findViewById<android.widget.TextView>(R.id.dialogMessage)
        
        // Set message with product name if available
        scannedItemTitle?.let {
            dialogMessage.text = getString(R.string.enter_barcode_manually) + " - $it"
        }
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnConfirm.setOnClickListener {
            val barcode = barcodeEditText.text?.toString()?.trim()
            if (barcode.isNullOrEmpty()) {
                barcodeEditText.error = getString(R.string.please_enter_barcode)
            } else {
                dialog.dismiss()
                processBarcode(barcode)
            }
        }
        
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            flag = it.getInt(ARG_FLAG)
            orderId = it.getString(ARG_ORDER_ID)
            category = it.getString(ARG_CATEGORY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopperOrderProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!orderId.isNullOrEmpty()) {
            val type = when (flag) {
                1 -> "1"
                2 -> "2"
                else -> "1" // fallback if flag is unexpected
            }
            viewmodel.retrieveShopperOrderProducts(
                orderId!!,
                type,
            )
            if (type == "2") {
                binding.endOrderBtn.visibility = View.VISIBLE
            } else {
                binding.endOrderBtn.visibility = View.GONE
            }
        } else {
            Toast.makeText(requireContext(), "Missing required arguments", Toast.LENGTH_SHORT)
                .show()
        }

        binding.endOrderBtn.setOnClickListener {
            viewmodel.retrieveStartEndEmployeeOrder(
                order_id = orderId.toString(),
                type_flag = "1",
                start_flag = "2"
            )
        }

        getOrderProducts()
        getSetShopperOrder()
        getStartEndEmployeeOrder()
    }

    private fun getOrderProducts() {
        viewmodel.getShopperOrderProducts().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    val adapter = ShopperOrderProductsAdapter(
                        list = result.data.data.items,
                        category = category.toString(),
                        onAvailableClick = { item ->
                            scannedItemTitle = item.title
                            scannedItemId = item.order_item_id
                            val integrator = IntentIntegrator(requireActivity())
                            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                            integrator.setPrompt("Scan barcode for ${item.title}")
                            integrator.setCameraId(0)
                            integrator.setBeepEnabled(true)
                            integrator.setBarcodeImageEnabled(true)
                            integrator.setCaptureActivity(CaptureActivityPortrait::class.java)
                            scanLauncher.launch(integrator.createScanIntent())
                        },
                        onUnAvailableClick = { item ->
                            viewmodel.retrieveSetShopperOrder(
                                orderId.toString(),
                                scannedItemId.toString(),
                                "0",
                                item.sku
                            )

                        }
                    )
                    binding.productsRv.layoutManager = LinearLayoutManager(requireContext())
                    binding.productsRv.adapter = adapter
                }

                is NetworkResults.Error -> {
                    Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> { /* Loading or other states */
                }
            }
        }
    }

    private fun getSetShopperOrder() {
        viewmodel.getSetShopperOrder().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        Toast.makeText(
                            requireContext(),
                            result.data.msg.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        val type = when (flag) {
                            1 -> "1"
                            2 -> "2"
                            else -> "1"
                        }
                        viewmodel.retrieveShopperOrderProducts(
                            orderId!!,
                            type,
                        )

                    } else {
                        Toast.makeText(
                            requireContext(),
                            result.data.msg.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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

    private fun getStartEndEmployeeOrder() {
        viewmodel.getStartEndEmployeeOrder().observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        showToast(requireContext(), result.data.msg.message)
                        val intent = Intent(requireContext(), ShopperHomeActivity::class.java)
                        startActivity(intent)
                        activity?.finishAffinity()
                    } else {
                        showErrorToast(requireContext(), result.data.msg.message)
                    }
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

    override fun onResume() {
        super.onResume()
        if (!orderId.isNullOrEmpty()) {
            val type = when (flag) {
                1 -> "1"
                2 -> "2"
                else -> "1" // fallback if flag is unexpected
            }
            viewmodel.retrieveShopperOrderProducts(
                orderId!!,
                type,
            )
            getOrderProducts()
        } else {
            Toast.makeText(requireContext(), "Missing required arguments", Toast.LENGTH_SHORT)
                .show()
        }
    }
}