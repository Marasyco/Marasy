package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.CartItemsAdapter
import com.blueray.marasy.databinding.ActivityCartBinding
import com.blueray.marasy.interfaces.CartItemListener
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class CartActivity : BaseActivity() {
    private lateinit var binding: ActivityCartBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var adapter: CartItemsAdapter
    private var orderId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        if (com.blueray.marasy.helpers.HelperUtils.isGuest(this)) {
            com.blueray.marasy.helpers.HelperUtils.showLoginRequiredDialog(this) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            return
        }

        binding.includedTab.title.text = getString(R.string.cart)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }
        binding.includedTab.title.text = getString(R.string.cart)

        binding.updateCartBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        binding.checkoutBtn.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("orderId", orderId)
            intent.putExtra("totalPrice", binding.totalAmount.text.toString() + " JD")
            intent.putExtra("deliveryFees", "0.00 JD")
            startActivity(intent)
        }

        viewmodel.retrieveCart()


        getCart()
        getUpdateCart()
        getDeleteCartItem()
    }

    private fun getCart() {
        viewmodel.getViewCart().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {

                        orderId = result.data.data.order_id
                        binding.totalAmount.text = result.data.data.total_order_price + " JD"

                        if (result.data.data.items.isEmpty()) {
                            binding.noItemsTv.visibility = View.VISIBLE
                            binding.productsRv.visibility = View.GONE
                        } else {
                            binding.noItemsTv.visibility = View.GONE
                            binding.productsRv.visibility = View.VISIBLE
                        }


                        adapter = CartItemsAdapter(object : CartItemListener {
                            override fun onQuantityChange(
                                itemId: String,
                                quantity: String,
                            ) {
                                viewmodel.retrieveUpdateCart(
                                    orderId,
                                    itemId,
                                    quantity,
                                )
                            }

                            override fun onItemDelete(itemId: String) {
                                viewmodel.retrieveDeleteCartItem(
                                    itemId
                                )
                            }

                        })
                        adapter.submitList(result.data.data.items)
                        binding.productsRv.adapter = adapter
                        binding.productsRv.layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    } else {
                        binding.noItemsTv.visibility = View.VISIBLE
                        binding.totalAmount.text = "0.00 JD"

                    }
                }

                is NetworkResults.Error -> {

                }

                else -> {

                }
            }
        }
    }

    private fun getUpdateCart() {
        viewmodel.getUpdateCart().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                    viewmodel.retrieveCart()
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

    private fun getDeleteCartItem() {
        viewmodel.getDeleteCartItem().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                    if (adapter.currentList.size == 1) {
                        adapter.submitList(listOf())
                        adapter.notifyDataSetChanged()
                        viewmodel.retrieveCart()
                    }
                    viewmodel.retrieveCart()
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