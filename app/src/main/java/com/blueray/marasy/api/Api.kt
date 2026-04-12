package com.blueray.marasy.api

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
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface Api {

    @Multipart
    @POST("app/login-user")
    suspend fun loginUser(
        @Part("phone") phone: RequestBody,
        @Part("lang") lang: RequestBody
    ): Response<MessageResponse>

    @Multipart
    @POST("app/login-user")
    suspend fun loginUserWithOTP(
        @Part("phone") phone: RequestBody,
        @Part("otp") otp: RequestBody,
        @Part("lang") lang: RequestBody
    ): Response<UserLoginResponse>

    @POST("app/slider1InHomePage")
    suspend fun homeSlider(): HomeSliderResponse

    @POST("app/get-main-department")
    suspend fun getCategories(): CategoriesResponse

    @POST("app/getTradeMarks")
    suspend fun getTrademarks(): TrademarksResponse

    @Multipart
    @POST("app/get-sub-department")
    suspend fun getSubCategories(
        @Part("uid") uid: RequestBody,
        @Part("parent_id") parent_id: RequestBody
    ): GetSubCategoriesResponse

    @Multipart
    @POST("app/get-product")
    suspend fun getProductDetails(
        @Part("uid") uid: RequestBody,
        @Part("pid") pid: RequestBody
    ): ProductDetailsResponse

    @Multipart
    @POST("app/get-products")
    suspend fun getCategoryProducts(
        @Part("uid") uid: RequestBody,
        @Part("category") category: RequestBody,
        @Part("sub_category") sub_category: RequestBody,
        @Part("page") page: RequestBody,
    ): GetProductsResponse

    @Multipart
    @POST("app/get-products")
    suspend fun getTrademarkProducts(
        @Part("uid") uid: RequestBody,
        @Part("trade_mark") trade_mark: RequestBody,
        @Part("page") page: RequestBody,
    ): GetProductsResponse

    @Multipart
    @POST("app/add-favorite")
    suspend fun addToFavorite(
        @Part("uid") uid: RequestBody,
        @Part("product_id") product_id: RequestBody,
        @Part("lang") lang: RequestBody,
    ): MessageResponse

    @Multipart
    @POST("app/add-to-cart")
    suspend fun addToCart(
        @Part("vid") apiPassword: RequestBody,
        @Part("uid") userId: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part("price") productId: RequestBody,
        @Part("lang") language: RequestBody,

        ): MessageResponse

    @Multipart
    @POST("app/get-fav-products")
    suspend fun getFavoriteProducts(
        @Part("uid") uid: RequestBody
    ): GetFavoriteProductsResponse

    @Multipart
    @POST("getMyNotifications")
    suspend fun getMyNotifications(
        @Part("uid") uid: RequestBody
    ): GetNotificationResponse

    @Multipart
    @POST("app/view-cart")
    suspend fun viewCart(
        @Part("uid") uid: RequestBody
    ): ViewCartResponse

    @Multipart
    @POST("app/update-cart")
    suspend fun updateCart(
        @Part("uid") uid: RequestBody,
        @Part("order_id") order_id: RequestBody,
        @Part("order_item_id") order_item_id: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part("lang") lang: RequestBody,
    ): MessageResponse

    @Multipart
    @POST("app/remove-cart")
    suspend fun deleteCartItem(
        @Part("uid") uid: RequestBody,
        @Part("order_item_id") order_item_id: RequestBody,
        @Part("lang") lang: RequestBody,
    ): MessageResponse

    @Multipart
    @POST("app/get-my-address")
    suspend fun getMyAddresses(
        @Part("uid") uid: RequestBody
    ): GetMyAddressResponse


    @Multipart
    @POST("app/view-user-profile")
    suspend fun viewProfile(
        @Part("uid") uid: RequestBody
    ): ViewProfileResponse

    @Multipart
    @POST("app/checkout")
    suspend fun checkout(
        @Part("uid") uid: RequestBody,
        @Part("order_id") order_id: RequestBody,
        @Part("payment_method") payment_method: RequestBody,
        @Part("lang") lang: RequestBody,
    ): Response<MessageResponse>

    @Multipart
    @POST("app/get-products")
    suspend fun searchProducts(
        @Part("uid") uid: RequestBody,
        @Part("search_text") search_text: RequestBody,
        @Part("page") page: RequestBody,
        @Part("search_flag") search_flag: RequestBody
    ): GetProductsResponse


    @Multipart
    @POST("app/checkPhone")
    suspend fun checkPhone(
        @Part("phone") phone: RequestBody,
        //if  flag = 0  then it checks if phone is used , if flag = 1 then it sends otp to the number
        @Part("flag") flag: RequestBody,
        @Part("lang") lang: RequestBody,
    ): MessageResponse

    @POST("app/getSectors")
    suspend fun getSectors(

    ): GetSectorsResponse

    @Multipart
    @POST("app/getSectors")
    suspend fun getSubSectors(
        @Part("parent_tid") parent_tid: RequestBody
    ): GetSectorsResponse

    @Multipart
    @POST("app/getCities")
    suspend fun getAreas(
        @Part("parent_tid") parent_tid: RequestBody
    ): GetCitiesResponse

    @POST("app/getCities")
    suspend fun getCities(): GetCitiesResponse

    @Multipart
    @POST("app/add-user")
    suspend fun addUser(
        @Part("lang") lang: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("full_name") full_name: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody,
        @Part("player_id") playerId: RequestBody,
        @Part("detailed_address") detailed_address: RequestBody,
        @Part("city") city: RequestBody,
        @Part("city_and_area") area: RequestBody,
        @Part("sector") sector: RequestBody,
        @Part("email") email: RequestBody,
        @Part("address_line1") address_line1: RequestBody,
    ): Response<LoginUserResponse>

    @Multipart
    @POST("/getBestSaling")
    suspend fun getBestSelling(
        @Part("uid") uid: RequestBody,
        @Part("lang") lang: RequestBody
    ): BestSellingResponse

    @Multipart
    @POST("app/edit-address-profile")
    suspend fun setDefaultAddress(
        @Part("uid") uid: RequestBody,
        @Part("profile_id") profile_id: RequestBody,
        @Part("as_default") as_default: RequestBody
    ): MessageResponse

    @Multipart
    @POST("app/delete-address-profile")
    suspend fun deleteAddress(
        @Part("uid") uid: RequestBody,
        @Part("profile_id") profile_id: RequestBody
    ): MessageResponse

    @Multipart
    @POST("app/add-address")
    suspend fun addAddress(
        @Part("uid") uid: RequestBody,
        @Part("address_line1") address_line1: RequestBody,
        @Part("detailed_address") detailed_address: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody,
        @Part("city_and_area") city_and_area: RequestBody,
    ): MessageResponse

    @POST("app/aboutUs")
    suspend fun aboutUs(

    ): AboutUsResponse

    @POST("app/privacyPolicies")
    suspend fun privacyPolicy(

    ): PrivacyPolicyResponse

    @POST("app/termsAndConditions")
    suspend fun termsAndConditions(

    ): PrivacyPolicyResponse

    @Multipart
    @POST("getMyWarranty")
    suspend fun getWarranties(
        @Part("uid") uid: RequestBody,
        @Part("lang") lang: RequestBody
    ): WarrantiesResponse

    @Multipart
    @POST("app/view-all-order")
    suspend fun viewOrders(
        @Part("uid") uid: RequestBody,
        @Part("order_state") order_state: RequestBody,
        @Part("lang") lang: RequestBody
    ): ViewOrdersResponse

    @Multipart
    @POST("app/login-employee")
    suspend fun employeeLogin(
        @Part("user_name") user_name: RequestBody,
        @Part("password") password: RequestBody,
        @Part("player_id") player_id: RequestBody,
        @Part("lang") lang: RequestBody,
    ): EmployeeLoginResponse

    @Multipart
    @POST("app/view-shoper-order")
    suspend fun viewShopperOrders(
        @Part("uid") uid: RequestBody,
        // 1 for waiting prepare 2 for done preparing
        @Part("order_state") order_state: RequestBody,
        @Part("lang") lang: RequestBody
    ): ShopperOrdersResponse

    @Multipart
    @POST("app/start-and-end-assembling-order")
    suspend fun startEndEmployeeOrder(
        @Part("uid") uid: RequestBody,
        @Part("order_id") order_id: RequestBody,
        // 1 to start 2 to end
        @Part("start_flag") start_flag: RequestBody,
        //  1 shopper 2 driver
        @Part("type_flag") type_flag: RequestBody,
        @Part("lang") lang: RequestBody,
    ): MessageResponse

    @Multipart
    @POST("app/set-shoper-order")
    suspend fun setShopperOrder(
        @Part("uid") uid: RequestBody,
        @Part("order_id") order_id: RequestBody,
        @Part("order_item_id") order_item_id: RequestBody,
        @Part("obtainable") obtainable: RequestBody,
        @Part("sku") sku: RequestBody,
        @Part("lang") lang: RequestBody,
    ): MessageResponse

    @Multipart
    @POST("app/view-shoper-order-details")
    suspend fun viewShopperOrderProducts(
        @Part("uid") uid: RequestBody,
        @Part("order_id") order_id: RequestBody,
        @Part("item_state") item_state: RequestBody,
        @Part("lang") lang: RequestBody
    ): ShopperOrderProducts

    @Multipart
    @POST("app/view-driver-order")
    suspend fun viewDriverOrders(
        @Part("uid") uid: RequestBody,
        // 1 for waiting delivery 2 for delivering 3 for done orders
        @Part("order_state") order_state: RequestBody,
        @Part("lang") lang: RequestBody,
    ): DriverOrdersResponse

    @Multipart
    @POST("app/view-driver-order-details")
    suspend fun viewDriverOrderDetails(
        @Part("uid") uid: RequestBody,
        @Part("order_id") order_id: RequestBody,
        @Part("lang") lang: RequestBody,
    ): DriverOrderDetailsResponse

    @Multipart
    @POST("app/get-address-details")
    suspend fun getAddressDetails(
        @Part("profile_id") profile_id: RequestBody
    ): GetAddressDetailsResponse

    @Multipart
    @POST("app/edit-address-profile")
    suspend fun editAddress(
        @Part("uid") uid: RequestBody,
        @Part("profile_id") profile_id: RequestBody,
        @Part("city_and_area") city_and_area: RequestBody,
        @Part("address_line1") address_line1: RequestBody,
        @Part("detailed_address") detailed_address: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody,
    ): MessageResponse

    @Multipart
    @POST("app/update-user")
    suspend fun updateUser(
        @Part("uid") uid: RequestBody,
        @Part("full_name") full_name: RequestBody,
        @Part("email") email: RequestBody,
    ): MessageResponse

    @Multipart
    @POST("app/get-offer-products")
    suspend fun getOfferProducts(
        @Part("uid") uid: RequestBody,
        @Part("lang") lang: RequestBody
    ): GetOfferProductsResponse

    @Multipart
    @POST("app/view-order-details")
    suspend fun viewOrderDetails(
        @Part("uid") uid: RequestBody,
        @Part("order_id") order_id: RequestBody
    ): ViewOrderDetailsResponse

    @Multipart
    @POST("app/reorder")
    suspend fun reOrder(
        @Part("uid") uid: RequestBody,
        @Part("order_id") order_id: RequestBody
    ): ReOrderResponse
}