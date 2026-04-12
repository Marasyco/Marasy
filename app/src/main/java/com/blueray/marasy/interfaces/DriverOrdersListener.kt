package com.blueray.marasy.interfaces

interface DriverOrdersListener {
    fun onOrderClick(id:String)
    fun onStartDeliveryClick(id:String , lat:Double, lon:Double)
}