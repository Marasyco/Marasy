package com.blueray.marasy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blueray.marasy.helpers.HelperUtils.getLang
import com.blueray.marasy.helpers.HelperUtils.getUID
import com.blueray.marasy.model.AboutUsResponse
import com.blueray.marasy.model.BestSellingResponse
import com.blueray.marasy.model.CategoriesResponse
import com.blueray.marasy.model.DriverOrderDetailsResponse
import com.blueray.marasy.model.DriverOrdersResponse
import com.blueray.marasy.model.EmployeeLoginResponse
import com.blueray.marasy.model.GetAddressDetailsResponse
import com.blueray.marasy.model.GetCitiesResponse
import com.blueray.marasy.model.GetFavoriteProductsResponse
import com.blueray.marasy.model.GetMyAddressResponse
import com.blueray.marasy.model.GetNotificationResponse
import com.blueray.marasy.model.GetOfferProductsResponse
import com.blueray.marasy.model.GetProductsResponse
import com.blueray.marasy.model.GetSectorsResponse
import com.blueray.marasy.model.GetSubCategoriesResponse
import com.blueray.marasy.model.HomeSliderResponse
import com.blueray.marasy.model.LoginUserResponse
import com.blueray.marasy.model.MessageResponse
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.model.PrivacyPolicyResponse
import com.blueray.marasy.model.ProductDetailsResponse
import com.blueray.marasy.model.ShopperOrderProducts
import com.blueray.marasy.model.ShopperOrdersResponse
import com.blueray.marasy.model.UserLoginResponse
import com.blueray.marasy.model.ViewCartResponse
import com.blueray.marasy.model.ViewOrdersResponse
import com.blueray.marasy.model.ViewOrderDetailsResponse
import com.blueray.marasy.model.ReOrderResponse
import com.blueray.marasy.model.TrademarksResponse
import com.blueray.marasy.model.ViewProfileResponse
import com.blueray.marasy.model.WarrantiesResponse
import com.blueray.marasy.repo.Repository
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val repo = Repository
    val language = getLang(application.applicationContext)
    val uid = getUID(application.applicationContext)

    //user login
    private val loginUserLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveLoginUser(phone: String, deviceId: String = "") {
        viewModelScope.launch {
            loginUserLiveData.postValue(
                repo.loginUser(
                    phone,
                    language,
                    deviceId
                )
            )
        }
    }

    fun getLoginUser() = loginUserLiveData

    //user login with otp
    private val loginUserOtpLiveData = MutableLiveData<NetworkResults<UserLoginResponse>>()

    fun retrieveLoginUserOtp(phone: String, otp: String) {
        viewModelScope.launch {
            loginUserOtpLiveData.postValue(
                repo.loginUserWithOTP(
                    phone,
                    otp,
                    language
                )
            )
        }
    }

    fun getLoginUserOtp() = loginUserOtpLiveData

    //home slider
    private val homeSliderLiveData = MutableLiveData<NetworkResults<HomeSliderResponse>>()

    fun retrieveHomeSlider() {
        viewModelScope.launch {
            homeSliderLiveData.postValue(
                repo.homeSlider(language)
            )
        }
    }

    fun getHomeSlider() = homeSliderLiveData

    //main categories
    private val mainCategoriesLiveData = MutableLiveData<NetworkResults<CategoriesResponse>>()

    fun retrieveMainCategories() {
        viewModelScope.launch {
            mainCategoriesLiveData.postValue(
                repo.getCategories(language)
            )
        }
    }

    fun getMainCategories() = mainCategoriesLiveData

    //trademarks
    private val trademarksLiveData = MutableLiveData<NetworkResults<TrademarksResponse>>()

    fun retrieveTrademarks() {
        viewModelScope.launch {
            trademarksLiveData.postValue(
                repo.getTrademarks(language)
            )
        }
    }

    fun getTrademarks() = trademarksLiveData

    //get sub categories
    private val subCategoriesLiveData = MutableLiveData<NetworkResults<GetSubCategoriesResponse>>()

    fun retrieveSubCategories(parentId: String) {
        viewModelScope.launch {
            subCategoriesLiveData.postValue(
                repo.getSubCategories(uid, parentId, language)
            )
        }
    }

    fun getSubCategories() = subCategoriesLiveData

    //get product details
    private val productDetailsLiveData = MutableLiveData<NetworkResults<ProductDetailsResponse>>()

    fun retrieveProductDetails(pid: String) {
        viewModelScope.launch {
            productDetailsLiveData.postValue(
                repo.getProductDetails(uid, pid, language)
            )
        }
    }

    fun getProductDetails() = productDetailsLiveData


    //get category products
    private val categoryProductsLiveData = MutableLiveData<NetworkResults<GetProductsResponse>>()

    fun retrieveCategoryProducts(
        category: String,
        subCategory: String,
        page: String
    ) {
        viewModelScope.launch {
            categoryProductsLiveData.postValue(
                repo.getCategoryProducts(
                    uid, category, subCategory, page, language
                )
            )
        }
    }

    fun getCategoryProducts() = categoryProductsLiveData

    //get trademark products
    private val trademarkProductsLiveData = MutableLiveData<NetworkResults<GetProductsResponse>>()

    fun retrieveTrademarkProducts(
        trademarkId: String,
        page: String
    ) {
        viewModelScope.launch {
            trademarkProductsLiveData.postValue(
                repo.getTrademarkProducts(
                    uid, trademarkId, page, language
                )
            )
        }
    }

    fun getTrademarkProducts() = trademarkProductsLiveData

    //add to favorite
    private val addToFavoriteLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveAddToFavorite(productId: String) {
        viewModelScope.launch {
            addToFavoriteLiveData.postValue(
                repo.addToFavorite(
                    uid,
                    productId,
                    language
                )
            )
        }
    }

    fun getAddToFavorite() = addToFavoriteLiveData


    //add to cart
    private val addToCartLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun addToCart(productId: String, price: String, quantity: String) {
        viewModelScope.launch {
            addToCartLiveData.value =
                repo.addToCart(productId, uid, quantity, price.toString(), language)
        }

    }

    fun getAddToCart() = addToCartLiveData

    // get favorite products
    private val favoriteProductsLiveData =
        MutableLiveData<NetworkResults<GetFavoriteProductsResponse>>()

    fun retrieveFavoriteProducts() {
        viewModelScope.launch {
            favoriteProductsLiveData.postValue(
                repo.getFavoriteProducts(uid, language)
            )
        }
    }

    fun getFavoriteProducts() = favoriteProductsLiveData

    //get my notifications
    private val notificationsLiveData = MutableLiveData<NetworkResults<GetNotificationResponse>>()

    fun retrieveMyNotifications() {
        viewModelScope.launch {
            notificationsLiveData.postValue(
                repo.getMyNotifications(uid, language)
            )
        }
    }

    fun getMyNotifications() = notificationsLiveData

    //view cart
    private val viewCartLiveData = MutableLiveData<NetworkResults<ViewCartResponse>>()

    fun retrieveCart() {
        viewModelScope.launch {
            viewCartLiveData.postValue(
                repo.viewCart(uid, language)
            )
        }
    }

    fun getViewCart() = viewCartLiveData

    //update cart

    private val updateCartLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveUpdateCart(
        order_id: String,
        order_item_id: String,
        quantity: String,
    ) {
        viewModelScope.launch {
            updateCartLiveData.postValue(
                repo.updateCart(
                    uid,
                    order_id,
                    order_item_id,
                    quantity,
                    language
                )
            )
        }
    }

    fun getUpdateCart() = updateCartLiveData

    //delete cart item
    private val deleteCartItemLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveDeleteCartItem(order_item_id: String) {
        viewModelScope.launch {
            deleteCartItemLiveData.postValue(
                repo.deleteCartItem(uid, order_item_id, language)
            )
        }
    }

    fun getDeleteCartItem() = deleteCartItemLiveData


    //get my addresses
    private val getMyAddressLiveData = MutableLiveData<NetworkResults<GetMyAddressResponse>>()

    fun retrieveMyAddresses() {
        viewModelScope.launch {
            getMyAddressLiveData.postValue(
                repo.getMyAddresses(uid, language)
            )
        }
    }

    fun getMyAddresses() = getMyAddressLiveData

    //view profile
    private val viewProfileLiveData = MutableLiveData<NetworkResults<ViewProfileResponse>>()

    fun retrieveViewProfile() {
        viewModelScope.launch {
            viewProfileLiveData.postValue(
                repo.viewProfile(uid, language)
            )
        }
    }

    fun getViewProfile() = viewProfileLiveData

    //checkout
    private val checkoutLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveCheckout(
        order_id: String,
        payment_method: String,
        note: String,
        ) {
        viewModelScope.launch {
            checkoutLiveData.postValue(
                repo.checkout(
                    uid,
                    order_id,
                    payment_method,
                    note,
                    language
                )
            )
        }
    }

    fun getCheckout() = checkoutLiveData

    fun clearCheckoutResult() {
        checkoutLiveData.value = null
    }

    //search
    private val searchLiveData = MutableLiveData<NetworkResults<GetProductsResponse>>()

    fun retrieveSearch(search_text: String, page: String, search_flag: String) {
        viewModelScope.launch {
            searchLiveData.postValue(
                repo.searchProducts(
                    uid, search_text, page, search_flag, language
                )
            )
        }
    }

    fun getSearch() = searchLiveData

    //check phone
    private val checkPhoneLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveCheckPhone(phone: String, flag: String) {
        viewModelScope.launch {
            checkPhoneLiveData.postValue(
                repo.checkPhone(
                    phone,
                    flag,
                    language
                )
            )
        }
    }

    fun getCheckPhone() = checkPhoneLiveData

    //get sectors
    private val sectorsLiveData = MutableLiveData<NetworkResults<GetSectorsResponse>>()

    fun retrieveSectors() {
        viewModelScope.launch {
            sectorsLiveData.postValue(
                repo.getSectors(language)
            )
        }
    }

    fun getSectors() = sectorsLiveData

    //get sub sectors
    private val subSectorsLiveData = MutableLiveData<NetworkResults<GetSectorsResponse>>()

    fun retrieveSubSectors(parent_tid: String) {
        viewModelScope.launch {
            subSectorsLiveData.postValue(
                repo.getSubSectors(parent_tid, language)
            )
        }
    }

    fun getSubSectors() = subSectorsLiveData

    //get areas for address
    private val getAreasLiveData = MutableLiveData<NetworkResults<GetCitiesResponse>>()

    fun retrieveAreas(parent_tid: String) {
        viewModelScope.launch {
            getAreasLiveData.postValue(
                repo.getAreas(parent_tid, language)
            )
        }
    }

    fun getAreas() = getAreasLiveData

    //get cities for address
    private val getCitiesLiveData = MutableLiveData<NetworkResults<GetCitiesResponse>>()

    fun retrieveCities() {
        viewModelScope.launch {
            getCitiesLiveData.postValue(
                repo.getCities(language)
            )
        }
    }

    fun getCities() = getCitiesLiveData

    // add user
    private val addUserLiveData = MutableLiveData<NetworkResults<LoginUserResponse>>()

    fun retrieveAddUser(
        phone: String,
        full_name: String,
        lat: String,
        lon: String,
        playerId: String,
        detailed_address: String,
        city: String,
        area: String,
        sector: String,
        email: String,
        address_line1: String
    ) {
        viewModelScope.launch {
            addUserLiveData.postValue(
                repo.addUser(
                    lang = language,
                    phone = phone,
                    full_name = full_name,
                    lat = lat,
                    lon = lon,
                    playerId = playerId,
                    detailed_address = detailed_address,
                    city = city,
                    area = area,
                    sector = sector,
                    email = email,
                    address_line1 = address_line1
                )
            )
        }
    }

    fun getAddUser() = addUserLiveData

    //get best selling
    private val bestSellingLiveData = MutableLiveData<NetworkResults<BestSellingResponse>>()

    fun retrieveBestSelling() {
        viewModelScope.launch {
            bestSellingLiveData.postValue(
                repo.getBestSelling(uid, language)
            )
        }
    }

    fun getBestSelling() = bestSellingLiveData

    //set default address
    private val setDefaultAddressLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveSetDefaultAddress(
        profile_id: String,
    ) {
        viewModelScope.launch {
            setDefaultAddressLiveData.postValue(
                repo.setDefaultAddress(
                    uid,
                    profile_id,
                    "1",
                    language
                )
            )
        }
    }

    fun getSetDefaultAddress() = setDefaultAddressLiveData

    //delete address
    private val deleteAddressLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveDeleteAddress(
        profile_id: String
    ) {
        viewModelScope.launch {
            deleteAddressLiveData.postValue(
                repo.deleteAddress(
                    uid, profile_id, language
                )
            )
        }
    }

    fun getDeleteAddress() = deleteAddressLiveData

    //add address
    private val addAddressLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveAddAddress(
        address_line1: String,
        detailed_address: String,
        lat: String,
        lon: String,
        cityAndArea: String,
    ) {
        viewModelScope.launch {
            addAddressLiveData.postValue(
                repo.addAddress(
                    uid = uid,
                    address_line1 = address_line1,
                    detailed_address = detailed_address,
                    lat = lat,
                    lon = lon,
                    city_and_area = cityAndArea,
                    lang = language,
                )
            )
        }
    }

    fun getAddAddress() = addAddressLiveData

    //about us
    private val aboutUsLiveData = MutableLiveData<NetworkResults<AboutUsResponse>>()

    fun retrieveAboutUs() {
        viewModelScope.launch {
            aboutUsLiveData.postValue(
                repo.aboutUs(language)
            )
        }
    }

    fun getAboutUs() = aboutUsLiveData

    //privacy policy
    private val privacyPolicyLiveData = MutableLiveData<NetworkResults<PrivacyPolicyResponse>>()

    fun retrievePrivacyPolicy() {
        viewModelScope.launch {
            privacyPolicyLiveData.postValue(
                repo.privacyPolicy(language)
            )
        }
    }

    fun getPrivacyPolicy() = privacyPolicyLiveData

    //terms And Conditions
    private val termsAndConditionsLiveData =
        MutableLiveData<NetworkResults<PrivacyPolicyResponse>>()

    fun retrieveTermsAndConditions() {
        viewModelScope.launch {
            termsAndConditionsLiveData.postValue(
                repo.termsAndConditions(language)
            )
        }
    }

    fun getTermsAndConditions() = termsAndConditionsLiveData

    //get warranties
    private val warrantiesLiveData = MutableLiveData<NetworkResults<WarrantiesResponse>>()

    fun retrieveWarranties() {
        viewModelScope.launch {
            warrantiesLiveData.postValue(
                repo.getWarranties(uid, language)
            )
        }
    }

    fun getWarranties() = warrantiesLiveData

    //view orders
    private val viewOrdersLiveData = MutableLiveData<NetworkResults<ViewOrdersResponse>>()

    fun retrieveViewOrders(order_state: String) {
        viewModelScope.launch {
            viewOrdersLiveData.postValue(
                repo.viewOrders(
                    uid,
                    order_state,
                    language
                )
            )
        }
    }

    fun getViewOrders() = viewOrdersLiveData


    //employee login
    private val employeeLoginLiveData = MutableLiveData<NetworkResults<EmployeeLoginResponse>>()

    fun retrieveEmployeeLogin(
        userName: String,
        password: String,
        playerId: String
    ) {
        viewModelScope.launch {
            employeeLoginLiveData.postValue(
                repo.employeeLogin(
                    userName,
                    password,
                    playerId,
                    language
                )
            )
        }
    }

    fun getEmployeeLogin() = employeeLoginLiveData

    //view shopper orders
    private val viewShopperOrdersLiveData = MutableLiveData<NetworkResults<ShopperOrdersResponse>>()

    fun retrieveShopperOrders(
        order_state: String
    ) {
        viewModelScope.launch {
            viewShopperOrdersLiveData.postValue(
                repo.viewShopperOrders(
                    uid,
                    order_state,
                    language
                )
            )
        }
    }

    fun getShopperOrders() = viewShopperOrdersLiveData

    //start end order
    private val startEndEmployeeOrderLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveStartEndEmployeeOrder(
        order_id: String,
        start_flag: String,
        type_flag: String
    ) {
        viewModelScope.launch {
            startEndEmployeeOrderLiveData.postValue(
                repo.startEndEmployeeOrder(
                    uid,
                    order_id,
                    start_flag,
                    type_flag,
                    language
                )
            )
        }
    }

    fun getStartEndEmployeeOrder() = startEndEmployeeOrderLiveData

    //set shopper order
    private val setShopperOrderLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveSetShopperOrder(
        order_id: String,
        order_item_id: String,
        obtainable: String,
        sku: String
    ) {
        viewModelScope.launch {
            setShopperOrderLiveData.postValue(
                repo.setShopperOrder(
                    uid, order_id, order_item_id, obtainable, sku, language
                )
            )
        }
    }

    fun getSetShopperOrder() = setShopperOrderLiveData

    //view shopper order products
    private val shopperOrderProductsLiveData =
        MutableLiveData<NetworkResults<ShopperOrderProducts>>()

    fun retrieveShopperOrderProducts(
        order_id: String,
        item_state: String,
    ) {
        viewModelScope.launch {
            shopperOrderProductsLiveData.postValue(
                repo.viewShopperOrderProducts(
                    uid, order_id, item_state, language
                )
            )
        }
    }

    fun getShopperOrderProducts() = shopperOrderProductsLiveData

    //view driver orders
    private val viewDriverOrdersLiveData = MutableLiveData<NetworkResults<DriverOrdersResponse>>()

    fun retrieveDriverOrders(order_state: String) {
        viewModelScope.launch {
            viewDriverOrdersLiveData.postValue(
                repo.viewDriverOrders(
                    uid,
                    order_state,
                    language
                )
            )
        }
    }

    fun getDriverOrders() = viewDriverOrdersLiveData

    //view driver order details
    private val driverOrderDetailsLiveData =
        MutableLiveData<NetworkResults<DriverOrderDetailsResponse>>()

    fun retrieveDriverOrderDetails(order_id: String) {
        viewModelScope.launch {
            driverOrderDetailsLiveData.postValue(
                repo.viewDriverOrderDetails(uid, order_id, language)
            )
        }
    }

    fun getDriverOrderDetails() = driverOrderDetailsLiveData

    //get address details
    private val getAddressDetailsLiveData =
        MutableLiveData<NetworkResults<GetAddressDetailsResponse>>()

    fun retrieveAddressDetails(
        profile_id: String
    ) {
        viewModelScope.launch {
            getAddressDetailsLiveData.postValue(
                repo.getAddressDetails(profile_id, language)
            )
        }
    }

    fun getAddressDetails() = getAddressDetailsLiveData

    //edit address
    private val editAddressLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveEditAddress(
        profile_id: String,
        city_and_area: String,
        address_line1: String,
        detailed_address: String,
        lat: String,
        lon: String,
    ) {
        viewModelScope.launch {
            editAddressLiveData.postValue(
                repo.editAddress(
                    uid, profile_id, city_and_area, address_line1, detailed_address, lat, lon, language
                )
            )
        }
    }

    fun getEditAddress() = editAddressLiveData

    //update user
    private val updateUserLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun retrieveUpdateUser(
        full_name: String,
        email: String,
    ) {
        viewModelScope.launch {
            updateUserLiveData.postValue(
                repo.updateUser(
                    uid,
                    full_name,
                    email,
                    language,
                )
            )
        }
    }

    fun getUpdateUser() = updateUserLiveData

    //get offer products
    private val offerProductsLiveData = MutableLiveData<NetworkResults<GetOfferProductsResponse>>()

    fun retrieveOfferProducts() {
        viewModelScope.launch {
            offerProductsLiveData.postValue(
                repo.getOfferProducts(uid, language)
            )
        }
    }

    fun getOfferProducts() = offerProductsLiveData

    //view order details
    private val viewOrderDetailsLiveData =
        MutableLiveData<NetworkResults<ViewOrderDetailsResponse>>()

    fun retrieveOrderDetails(
        order_id: String
    ) {
        viewModelScope.launch {
            viewOrderDetailsLiveData.postValue(
                repo.viewOrderDetails(
                    uid, order_id, language
                )
            )
        }
    }

    fun getOrderDetails() = viewOrderDetailsLiveData

    //reorder
    private val reOrderLiveData = MutableLiveData<NetworkResults<ReOrderResponse>>()

    fun retrieveReOrder(order_id: String) {
        viewModelScope.launch {
            reOrderLiveData.postValue(
                repo.reOrder(
                    uid,
                    order_id,
                    language
                )
            )
        }
    }

    fun getReOrder() = reOrderLiveData

    private val contactUsLiveData = MutableLiveData<NetworkResults<MessageResponse>>()

    fun submitContactUs(phone: String, notes: String, fullName: String) {
        viewModelScope.launch {
            contactUsLiveData.postValue(
                repo.contactUs(phone, notes, fullName, language)
            )
        }
    }

    fun getContactUs() = contactUsLiveData
}