package com.blueray.marasy.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.AttributesAdapter
import com.blueray.marasy.adapters.BestSellingAdapter
import com.blueray.marasy.databinding.ActivityProductDetailsBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.helpers.HelperUtils.isGuest
import com.blueray.marasy.helpers.HelperUtils.showLoginRequiredDialog
import com.blueray.marasy.helpers.HelperUtils.showToast
import com.blueray.marasy.interfaces.OnBestSellingClick
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.model.Variation
import com.blueray.marasy.viewmodel.AppViewModel
import com.bumptech.glide.Glide
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min

class ProductDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityProductDetailsBinding
    private val viewmodel by viewModels<AppViewModel>()
    private var attributesAdapter: AttributesAdapter? = null
    private lateinit var relatedProductsAdapter: BestSellingAdapter

    // Quantity state driven by the *selected* variation
    private var currentQuantity = 1
    private var currentMinQty = 1
    private var currentMaxQty: Int? = null  // null = no explicit max
    private var vid = ""
    private var pid = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        pid = intent.getStringExtra("pid").toString()
        // Initial UI state
        bindQuantityUI()

        binding.backButton.setOnClickListener {
            finish()
        }
        
        // Setup collapsible cards
        setupCollapsibleCards()

        // Stepper listeners (will respect min/max once data is loaded)
        binding.minusButton.setOnClickListener {
            val newQ = max(currentMinQty, currentQuantity - 1)
            if (newQ != currentQuantity) {
                currentQuantity = newQ
                bindQuantityUI()
            }
        }

        binding.favoriteCheckBox.setOnClickListener {
            if (HelperUtils.isGuest(this)) {
                HelperUtils.showLoginRequiredDialog(this) {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            } else {
                viewmodel.retrieveAddToFavorite(pid)
            }
        }

        binding.plusButton.setOnClickListener {
            val maxAllowed = currentMaxQty ?: Int.MAX_VALUE
            val newQ = min(maxAllowed, currentQuantity + 1)
            if (newQ != currentQuantity) {
                currentQuantity = newQ
                bindQuantityUI()
            } else if (currentMaxQty != null) {
                showErrorToast(this, getString(R.string.reached_max_quantity))
            }
        }

        binding.addToCartButton.setOnClickListener {
            if (HelperUtils.isGuest(this)) {
                HelperUtils.showLoginRequiredDialog(this) {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            } else {
                // Check if a variation is selected (vid is not empty)
                Log.d("****vid3", vid)
                if (vid.isEmpty()) {
                    showErrorToast(this, getString(R.string.please_select_all_options))
                    return@setOnClickListener
                }

                viewmodel.addToCart(
                    vid,
                    formatPrice(binding.priceTv.text.toString()),
                    currentQuantity.toString()
                )
            }
        }

        // Load product details
        showLoading()
        viewmodel.retrieveProductDetails(pid)
        observeProductDetails()
        observeAddToCart()
        observeAddToFavorite()
    }
    
    private fun showLoading() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.main.visibility = View.GONE
    }
    
    private fun hideLoading() {
        binding.loadingProgressBar.visibility = View.GONE
        binding.main.visibility = View.VISIBLE
    }

    private fun observeProductDetails() {
        viewmodel.getProductDetails().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    hideLoading()
                    if (result.data.msg.status == 200) {
                        val product = result.data.data

                        Glide.with(this)
                            .load(HelperUtils.BASE_URL + product.images)
                            .placeholder(R.drawable.marasy_logo)
                            .into(binding.image)

                        binding.name.text = product.title
                        
                        // Handle description with HTML
                        if (product.body.isNotBlank()) {
                            binding.descriptionCard.visibility = View.VISIBLE
                            setupDescription(product.body)
                        } else {
                            binding.descriptionCard.visibility = View.GONE
                        }
                        
                        // Handle steps for use
                        product.steps_for_use?.let { steps ->
                            if (steps.isNotBlank()) {
                                binding.stepsForUseCard.visibility = View.VISIBLE
                                setupStepsForUse(steps)
                            } else {
                                binding.stepsForUseCard.visibility = View.GONE
                            }
                        } ?: run {
                            binding.stepsForUseCard.visibility = View.GONE
                        }
                        
                        // Handle features
                        product.features?.let { features ->
                            if (features.isNotBlank()) {
                                binding.featuresCard.visibility = View.VISIBLE
                                setupFeatures(features)
                            } else {
                                binding.featuresCard.visibility = View.GONE
                            }
                        } ?: run {
                            binding.featuresCard.visibility = View.GONE
                        }

                        if (product.fav == 1) binding.favoriteCheckBox.isChecked = true

                        // Handle unknown_price
                        if (product.unknown_price == 1) {

                            // Hide attributes and add to cart
                            binding.attributesRv.visibility = View.GONE
                            binding.addToCartCard.visibility = View.GONE
                            binding.priceTv.visibility = View.GONE
                        } else {
                            // Show normal UI
                            binding.attributesRv.visibility = View.VISIBLE
                            binding.addToCartCard.visibility = View.VISIBLE
                            binding.priceTv.visibility = View.VISIBLE
                            
                            val attributesList = product.attributes.toMutableList()
                            val variationsList = product.variations

                            Log.d("****vid attributesList", attributesList.toString())
                            Log.d("****vid variationsList", variationsList.toString())
                            // Check if attributes list is empty
                        if (attributesList.isEmpty() && variationsList.isNotEmpty()) {
                            // No attributes - use first variation directly
                            binding.attributesRv.visibility = android.view.View.GONE
                            val firstVariation = variationsList.first()
                            applyVariationUI(firstVariation)
                        } else {
                            // Has attributes - show attribute selection
                            binding.attributesRv.visibility = android.view.View.VISIBLE
                            attributesAdapter = AttributesAdapter(
                                this,
                                attributesList,
                                variationsList,
                                onFiltered = { filtered ->
                                    val selected = attributesAdapter?.getSelectedVariation()
                                    if (selected != null) {
                                        applyVariationUI(selected)   // sets price + qty + vid
                                    } else {
                                        clearPrice()
                                        resetQuantityBoundsToDefault()
                                        vid = ""
                                        Log.d("****vid1", vid)
                                    }
                                }
                                // , placeholderLabel = "اختر"   // optional
                            )

                            binding.attributesRv.adapter = attributesAdapter
                            attributesAdapter?.initSelections()

                            // Initial state
                            attributesAdapter?.getSelectedVariation()?.let { v ->
                                Log.d("****vid0", v.toString())
                                applyVariationUI(v)
                            } ?: run {
                                clearPrice()
                                resetQuantityBoundsToDefault()
                            }
                        }
                        }
                        
                        // Setup related products
                        val relatedProducts = result.data.data.related
                        if (relatedProducts.isNotEmpty()) {
                            binding.relatedTv.visibility = View.VISIBLE
                            binding.relatedProductsRv.visibility = View.VISIBLE
                            relatedProductsAdapter = BestSellingAdapter(object : OnBestSellingClick {
                                override fun onProductDetailsClick(id: String) {
                                    val intent = Intent(
                                        this@ProductDetailsActivity,
                                        ProductDetailsActivity::class.java
                                    )
                                    intent.putExtra("pid", id)
                                    startActivity(intent)
                                }
                            })
                            relatedProductsAdapter.submitList(relatedProducts)
                            binding.relatedProductsRv.adapter = relatedProductsAdapter
                            binding.relatedProductsRv.layoutManager =
                                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        } else {
                            binding.relatedTv.visibility = View.GONE
                            binding.relatedProductsRv.visibility = View.GONE
                        }
                        
                        // Show WhatsApp card if unknown_price = 1 (always below related products)
                        if (product.unknown_price == 1) {
                            binding.whatsappCard.visibility = View.VISIBLE
                            binding.whatsappButton.setOnClickListener {
                                openWhatsAppWithMessage(product.title)
                            }
                        } else {
                            binding.whatsappCard.visibility = View.GONE
                        }

                    } else {
                        hideLoading()
                        showErrorToast(this, result.data.msg.message)
                    }
                }

                is NetworkResults.Error -> {
                    hideLoading()
                    showErrorToast(this, result.exception.localizedMessage.toString())
                }

                else -> {
                    hideLoading()
                }
            }
        }
    }

    private fun observeAddToCart() {
        viewmodel.getAddToCart().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 1) {
                        showToast(this, result.data.msg.message)
                        finish()
                    } else {
                        showErrorToast(this, result.data.msg.message)
                    }
                }

                is NetworkResults.Error -> {
                    showErrorToast(this, result.exception.localizedMessage.toString())
                }

                else -> Unit
            }
        }
    }

    private fun observeAddToFavorite() {
        viewmodel.getAddToFavorite().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        showToast(this, result.data.msg.message)
                    } else {
                        showErrorToast(this, result.data.msg.message)
                    }
                }

                is NetworkResults.Error -> {
                    showErrorToast(this, result.exception.localizedMessage.toString())
                }

                else -> Unit

            }
        }
    }


    /** ===================== UI helpers ===================== **/

    private fun applyVariationUI(v: Variation) {
        // Price + VID
        setPriceText(formatPrice(v.price))
        vid = v.vid
        Log.d("****vid2", v.toString())

        // Handle offer display
        if (v.offerFlag == 1 && v.old_price.isNotEmpty() && v.old_price != "0" && v.old_price != "null") {
            // Show discount UI
            val oldPrice = v.old_price.toBigDecimalOrNullSafe()
            val newPrice = v.price.toBigDecimalOrNullSafe()
            
            if (oldPrice != null && newPrice != null && oldPrice > newPrice) {
                // Show old price with strikethrough
                binding.oldPriceTv.visibility = View.VISIBLE
                binding.oldPriceTv.text = formatPrice(oldPrice)
                binding.oldPriceTv.paintFlags = binding.oldPriceTv.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                hideDiscountUI()
            }
        } else {
            hideDiscountUI()
        }

        // Quantity bounds from this variation
        val minQ = v.min_quantity.toIntOrNullSafe() ?: 1
        val maxQ = v.max_quantity.toIntOrNullSafe()  // can be null

        currentMinQty = minQ.coerceAtLeast(1)
        currentMaxQty = maxQ?.takeIf { it >= currentMinQty }

        // Clamp current quantity into [min, max]
        if (currentQuantity < currentMinQty) currentQuantity = currentMinQty
        currentMaxQty?.let { if (currentQuantity > it) currentQuantity = it }

        bindQuantityUI()
    }
    
    private fun hideDiscountUI() {
        binding.oldPriceTv.visibility = View.GONE
    }

    private fun clearPrice() {
        setPriceText("")
        hideDiscountUI()
    }

    private fun resetQuantityBoundsToDefault() {
        currentMinQty = 1
        currentMaxQty = null
        if (currentQuantity < currentMinQty) currentQuantity = currentMinQty
        bindQuantityUI()
    }

    private fun bindQuantityUI() {
        binding.quantity.text = currentQuantity.toString()
        binding.minusButton.isEnabled = currentQuantity > currentMinQty
        binding.plusButton.isEnabled = currentMaxQty?.let { currentQuantity < it } ?: true
    }

    private fun setPriceText(text: String) {
        binding.priceTv.text = text
    }

    /** ===================== utils ===================== **/

    private fun String?.toIntOrNullSafe(): Int? {
        // Handles null, "", "null"
        return this?.trim()?.takeIf { it.isNotEmpty() && it.lowercase() != "null" }?.toIntOrNull()
    }

    private fun String?.toBigDecimalOrNullSafe(): BigDecimal? {
        val clean =
            this?.trim()?.takeIf { it.isNotEmpty() && it.lowercase() != "null" } ?: return null
        return runCatching { BigDecimal(clean) }.getOrNull()
    }

    private fun formatPrice(raw: String): String {
        val bd = raw.toBigDecimalOrNullSafe() ?: return "$raw JD"
        return formatPrice(bd)
    }

    private fun formatPrice(bd: BigDecimal): String {
        val t = bd.stripTrailingZeros().toPlainString()
        return "$t JD"
    }
    
    private fun setupCollapsibleCards() {
        // Description card - starts collapsed
        var isDescriptionExpanded = false
        binding.descriptionContent.visibility = View.GONE
        binding.descriptionHeader.setOnClickListener {
            isDescriptionExpanded = !isDescriptionExpanded
            toggleCard(binding.descriptionContent, binding.descriptionExpandIcon, isDescriptionExpanded)
        }

        // Steps for use card - starts collapsed
        var isStepsExpanded = false
        binding.stepsForUseHeader.setOnClickListener {
            isStepsExpanded = !isStepsExpanded
            toggleCard(binding.stepsForUseContent, binding.stepsForUseExpandIcon, isStepsExpanded)
        }
        
        // Features card - starts collapsed
        var isFeaturesExpanded = false
        binding.featuresHeader.setOnClickListener {
            isFeaturesExpanded = !isFeaturesExpanded
            toggleCard(binding.featuresContent, binding.featuresExpandIcon, isFeaturesExpanded)
        }
    }
    
    private fun toggleCard(contentView: View, iconView: View, isExpanded: Boolean) {
        val animation = if (isExpanded) {
            AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        } else {
            AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
        }
        
        if (isExpanded) {
            contentView.visibility = View.VISIBLE
        } else {
            contentView.visibility = View.GONE
        }
        contentView.startAnimation(animation)
        
        // Rotate icon
        val rotateAnimation = RotateAnimation(
            if (isExpanded) 0f else 180f,
            if (isExpanded) 180f else 0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            fillAfter = true
        }
        iconView.startAnimation(rotateAnimation)
    }
    
    private fun setupDescription(htmlContent: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            binding.descriptionTv.text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            binding.descriptionTv.text = Html.fromHtml(htmlContent)
        }
    }
    
    private fun setupStepsForUse(htmlContent: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            binding.stepsForUseTv.text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            binding.stepsForUseTv.text = Html.fromHtml(htmlContent)
        }
    }
    
    private fun setupFeatures(htmlContent: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            binding.featuresTv.text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            binding.featuresTv.text = Html.fromHtml(htmlContent)
        }
    }
    
    private fun openWhatsAppWithMessage(productName: String) {
        // TODO: Replace with actual support phone number from API or config
        val supportPhoneNumber = "962789105999" // Placeholder - should be configurable
        val message = getString(R.string.i_want_to_ask_about_this_product, productName)
        val encodedMessage = Uri.encode(message)
        
        try {
            val uri = Uri.parse("https://wa.me/$supportPhoneNumber?text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.whatsapp")
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web WhatsApp if app not installed
            try {
                val uri = Uri.parse("https://wa.me/$supportPhoneNumber?text=$encodedMessage")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(this, getString(R.string.whatsapp_not_installed), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
