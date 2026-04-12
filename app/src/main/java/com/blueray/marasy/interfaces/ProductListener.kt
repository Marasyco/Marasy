package com.blueray.marasy.interfaces

import com.blueray.marasy.model.Product

interface ProductListener {
    fun onProductClick(product: Product)
    fun onFavoriteToggle(product: Product, isChecked: Boolean)
}