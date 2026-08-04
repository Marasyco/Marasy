package com.blueray.marasy.ui.shopper

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityShopperOrderDetailsBinding
import com.blueray.marasy.helpers.ViewUtils.hide
import com.blueray.marasy.helpers.ViewUtils.show
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.activities.BaseActivity
import com.blueray.marasy.viewmodel.AppViewModel

class ShopperOrderDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityShopperOrderDetailsBinding
    private val viewmodel by viewModels<AppViewModel>()

    var orderId = ""
    var paymentWay = ""
    var numberOfItems = ""
    var price = ""
    var notes = ""
    var flag = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopperOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        orderId = intent.getStringExtra("orderId").toString()
        paymentWay = intent.getStringExtra("paymentWay").toString()
        numberOfItems = intent.getStringExtra("numberOfItems").toString()
        price = intent.getStringExtra("price").toString()
        notes = intent.getStringExtra("notes").toString()
        flag = intent.getStringExtra("flag").toString()


        binding.includedTab.backButton.setOnClickListener {
            finish()
        }
        binding.notesTv.text = notes
        binding.paymentWayTv.text = paymentWay
        binding.itemCountTv.text = numberOfItems
        binding.orderTotalTv.text = price + " JD"
        if (flag == "2") {
            binding.startShoppingButton.hide()
        } else {
            binding.startShoppingButton.show()
        }
        binding.startShoppingButton.setOnClickListener {
            binding.progress.show()
            binding.startShoppingButton.hide()
            viewmodel.retrieveStartEndEmployeeOrder(orderId, "1", "1")

        }
        getStartOrder()
    }

    private fun getStartOrder() {
        viewmodel.getStartEndEmployeeOrder().observe(this) { result ->
            binding.startShoppingButton.show()
            binding.progress.hide()
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {

                        Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ShopperOrderProductsActivity::class.java)
                        intent.putExtra("orderId", orderId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        this,
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