package com.blueray.marasy.interfaces

interface OnFavoriteProductClick {
    fun onProductDetailsClick(id: String)
    fun onFavoriteClick(id: String, pos: Int)
}