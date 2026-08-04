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
    private var deliveryTime = ""

    companion object {
        const val EXTRA_CHECKOUT_SUCCESS = "checkout_success"
        const val EXTRA_SUCCESS_MESSAGE = "success_message"
    }

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
            if (adapter.currentList.isEmpty()) {
                Toast.makeText(this, getString(R.string.cart_empty_message), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("orderId", orderId)
            intent.putExtra("totalPrice", binding.totalAmount.text.toString() + " JD")
            intent.putExtra("deliveryFees", "0.00 JD")
            intent.putExtra("deliveryTime", deliveryTime)
            startActivity(intent)
        }

        adapter = CartItemsAdapter(object : CartItemListener {
            override fun onQuantityChange(itemId: String, quantity: String) {
                viewmodel.retrieveUpdateCart(orderId, itemId, quantity)
            }

            override fun onItemDelete(itemId: String) {
                viewmodel.retrieveDeleteCartItem(itemId)
            }
        })
        binding.productsRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.productsRv.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewmodel.retrieveCart()
        }

        viewmodel.retrieveCart()

        getCart()
        getUpdateCart()
        getDeleteCartItem()
        handleCheckoutSuccessIfNeeded(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleCheckoutSuccessIfNeeded(intent)
    }

    private fun handleCheckoutSuccessIfNeeded(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_CHECKOUT_SUCCESS, false) != true) return

        val message = intent.getStringExtra(EXTRA_SUCCESS_MESSAGE)
            ?: getString(R.string.payment_success_message)
        intent.removeExtra(EXTRA_CHECKOUT_SUCCESS)
        intent.removeExtra(EXTRA_SUCCESS_MESSAGE)

        viewmodel.retrieveCart()

        com.blueray.marasy.helpers.HelperUtils.showPaymentSuccessDialog(this, message)
    }

    private fun getCart() {
        viewmodel.getViewCart().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    if (result.data.msg.status == 200) {

                        orderId = result.data.data.order_id
                        deliveryTime = result.data.data.time_to_deliverd
                        binding.totalAmount.text = result.data.data.total_order_price + " JD"

                        if (result.data.data.items.isEmpty()) {
                            binding.noItemsTv.visibility = View.VISIBLE
                            binding.productsRv.visibility = View.GONE
                            binding.checkoutBtn.isEnabled = false
                            binding.checkoutBtn.alpha = 0.5f
                        } else {
                            binding.noItemsTv.visibility = View.GONE
                            binding.productsRv.visibility = View.VISIBLE
                            binding.checkoutBtn.isEnabled = true
                            binding.checkoutBtn.alpha = 1.0f
                        }

                        adapter.submitList(result.data.data.items)
                    } else {
                        binding.noItemsTv.visibility = View.VISIBLE
                        binding.totalAmount.text = "0.00 JD"
                        binding.checkoutBtn.isEnabled = false
                        binding.checkoutBtn.alpha = 0.5f
                    }
                }

                is NetworkResults.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                else -> {}
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