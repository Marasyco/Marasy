package com.blueray.marasy.interfaces

interface CartItemListener {
    fun onQuantityChange(itemId:String , quantity:String )
    fun onItemDelete(itemId: String)
}