package com.blueray.marasy.repo

import android.util.Log
import android.util.Log.e
import com.blueray.marasy.api.ApiClient
import com.blueray.marasy.helpers.HelperUtils.toStringRequestBody
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
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

object Repository {

    suspend fun loginUser(
        phone: String,
        lang: String
    ): NetworkResults<MessageResponse> {

        val phoneBody = phone.toStringRequestBody()
        val langBody = lang.toStringRequestBody()
        try {
            val results = ApiClient.retrofitService.loginUser(
                phoneBody,
                langBody
            )
            return if (results.isSuccessful) {
                NetworkResults.Success(results.body()!!)
            } else {
                val errorBody = results.errorBody()?.string()
                errorBody?.let {
                    e("Repository Error Message", it)
                    try {
                        // Convert the error response JSON to a common Error Model
                        val apiResponse: MessageResponse =
                            Gson().fromJson(it, MessageResponse::class.java)
                        NetworkResults.ErrorMessage(apiResponse)
                    } catch (e: JsonSyntaxException) {
                        // Handle the case where the error response is not a valid JSON
                        NetworkResults.Error(e)
                    }
                } ?: NetworkResults.Error(Exception("Error body is null"))
            }
        } catch (e: Exception) {
            return NetworkResults.Error(e)
        }

    }

    suspend fun loginUserWithOTP(
        phone: String,
        otp: String,
        lang: String
    ): NetworkResults<UserLoginResponse> {
        val phoneBody = phone.toStringRequestBody()
        val otpBody = otp.toStringRequestBody()
        val langBody = lang.toStringRequestBody()
        try {
            val results = ApiClient.retrofitService.loginUserWithOTP(
                phoneBody,
                otpBody,
                langBody
            )
            return if (results.isSuccessful) {
                NetworkResults.Success(results.body()!!)
            } else {
                val errorBody = results.errorBody()?.string()
                errorBody?.let {
                    e("Repository Error Message", it)
                    try {
                        // Convert the error response JSON to a common Error Model
                        val apiResponse: MessageResponse =
                            Gson().fromJson(it, MessageResponse::class.java)
                        NetworkResults.ErrorMessage(apiResponse)
                    } catch (e: JsonSyntaxException) {
                        // Handle the case where the error response is not a valid JSON
                        NetworkResults.Error(e)
                    }
                } ?: NetworkResults.Error(Exception("Error body is null"))
            }
        } catch (e: Exception) {
            return NetworkResults.Error(e)
        }
    }

    suspend fun homeSlider(): NetworkResults<HomeSliderResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val results = ApiClient.retrofitService.homeSlider()
                NetworkResults.Success(results)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getCategories(): NetworkResults<CategoriesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val results = ApiClient.retrofitService.getCategories()
                NetworkResults.Success(results)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getTrademarks(): NetworkResults<TrademarksResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val results = ApiClient.retrofitService.getTrademarks()
                NetworkResults.Success(results)
            } catch (e: Exception) {
                Log.d("TRADEMARKS" , e.toString())
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getSubCategories(
        uid: String,
        parentId: String
    ): NetworkResults<GetSubCategoriesResponse> {
        val parentIdBody = parentId.toStringRequestBody()
        val uidBody = uid.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.getSubCategories(
                    uidBody,
                    parentIdBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getProductDetails(
        uid: String,
        pid: String
    ): NetworkResults<ProductDetailsResponse> {
        val pidBody = pid.toStringRequestBody()
        val uidBody = uid.toStringRequestBody()

        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.getProductDetails(
                    uidBody,
                    pidBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getCategoryProducts(
        uid: String,
        category: String,
        subCategory: String,
        page: String
    ): NetworkResults<GetProductsResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val categoryBody = category.toStringRequestBody()
            val subCategoryBody = subCategory.toStringRequestBody()
            val pageBody = page.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.getCategoryProducts(
                    uidBody,
                    categoryBody,
                    subCategoryBody,
                    pageBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getTrademarkProducts(
        uid: String,
        trademarkId: String,
        page: String
    ): NetworkResults<GetProductsResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val trademarkBody = trademarkId.toStringRequestBody()
            val pageBody = page.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.getTrademarkProducts(
                    uidBody,
                    trademarkBody,
                    pageBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun addToFavorite(
        uid: String,
        product_id: String,
        lang: String
    ): NetworkResults<MessageResponse> {
        val uidBody = uid.toStringRequestBody()
        val productIdBody = product_id.toStringRequestBody()
        val langBody = lang.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.addToFavorite(
                    uidBody,
                    productIdBody,
                    langBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun addToCart(
        vid: String,
        uid: String,
        quantity: String,
        price: String,
        lang: String,

        ): NetworkResults<MessageResponse> {
        return withContext(Dispatchers.IO) {
            val languageBody = lang.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val userIdBody = uid.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val productIdBody = vid.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val quantityBody = quantity.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val price = price.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            try {
                val results =

                    ApiClient.retrofitService.addToCart(
                        productIdBody,
                        userIdBody,
                        quantityBody,
                        price,
                        languageBody,

                        )
                NetworkResults.Success(results)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getFavoriteProducts(uid: String): NetworkResults<GetFavoriteProductsResponse> {
        val uidBody = uid.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.getFavoriteProducts(
                    uidBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getMyNotifications(uid: String): NetworkResults<GetNotificationResponse> {
        val uidBody = uid.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.getMyNotifications(uidBody)
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun viewCart(
        uid: String
    ): NetworkResults<ViewCartResponse> {
        val uidBody = uid.toStringRequestBody()

        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.viewCart(uidBody)
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun updateCart(
        uid: String,
        order_id: String,
        order_item_id: String,
        quantity: String,
        lang: String
    ): NetworkResults<MessageResponse> {
        val uidBody = uid.toStringRequestBody()
        val orderIdBody = order_id.toStringRequestBody()
        val orderItemIdBody = order_item_id.toStringRequestBody()
        val quantityBody = quantity.toStringRequestBody()
        val langBody = lang.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.updateCart(
                    uidBody,
                    orderIdBody,
                    orderItemIdBody,
                    quantityBody,
                    langBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun deleteCartItem(
        uid: String,
        order_item_id: String,
        lang: String
    ): NetworkResults<MessageResponse> {
        val uidBody = uid.toStringRequestBody()
        val orderItemIdBody = order_item_id.toStringRequestBody()
        val langBody = lang.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.deleteCartItem(
                    uidBody,
                    orderItemIdBody,
                    langBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }


    suspend fun getMyAddresses(
        uid: String
    ): NetworkResults<GetMyAddressResponse> {
        val uidBody = uid.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.getMyAddresses(uidBody)
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun viewProfile(
        uid: String
    ): NetworkResults<ViewProfileResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.viewProfile(uidBody)
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun checkout(
        uid: String,
        order_id: String,
        payment_method: String,
        lang: String
    ): NetworkResults<MessageResponse> {
        val uidBody = uid.toStringRequestBody()
        val orderIdBody = order_id.toStringRequestBody()
        val paymentMethodBody = payment_method.toStringRequestBody()

        val langBody = lang.toStringRequestBody()
        try {
            val result = ApiClient.retrofitService.checkout(
                uidBody,
                orderIdBody,
                paymentMethodBody,
                langBody
            )
            return if (result.isSuccessful) {
                NetworkResults.Success(result.body()!!)
            } else {
                val errorBody = result.errorBody()?.string()
                errorBody?.let {
                    e("Repository Error Message", it)
                    try {
                        // Convert the error response JSON to a common Error Model
                        val apiResponse: MessageResponse =
                            Gson().fromJson(it, MessageResponse::class.java)
                        NetworkResults.ErrorMessage(apiResponse)
                    } catch (e: JsonSyntaxException) {
                        // Handle the case where the error response is not a valid JSON
                        NetworkResults.Error(e)
                    }
                } ?: NetworkResults.Error(Exception("Error body is null"))
            }
        } catch (e: Exception) {
            return NetworkResults.Error(e)
        }

    }

    suspend fun searchProducts(
        uid: String,
        search_text: String,
        page: String,
        search_flag: String
    ): NetworkResults<GetProductsResponse> {
        val uidBody = uid.toStringRequestBody()
        val searchTextBody = search_text.toStringRequestBody()
        val pageBody = page.toStringRequestBody()
        val searchFlagBody = search_flag.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result =
                    ApiClient.retrofitService.searchProducts(
                        uidBody,
                        searchTextBody,
                        pageBody,
                        searchFlagBody
                    )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun checkPhone(
        phone: String,
        flag: String,
        lang: String
    ): NetworkResults<MessageResponse> {
        return withContext(Dispatchers.IO) {
            val phoneBody = phone.toStringRequestBody()
            val flagBody = flag.toStringRequestBody()
            val langBody = lang.toStringRequestBody()
            try {
                val results = ApiClient.retrofitService.checkPhone(
                    phoneBody, flagBody, langBody
                )
                NetworkResults.Success(results)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getSectors(): NetworkResults<GetSectorsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.getSectors()
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getSubSectors(parent_tid: String): NetworkResults<GetSectorsResponse> {
        return withContext(Dispatchers.IO) {
            val parentTidBody = parent_tid.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.getSubSectors(parentTidBody)
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getAreas(parent_tid: String): NetworkResults<GetCitiesResponse> {
        return withContext(Dispatchers.IO) {
            val parent_tidBody = parent_tid.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.getAreas(parent_tidBody)
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getCities(): NetworkResults<GetCitiesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.getCities()
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun addUser(
        phone: String,
        lang: String,
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
    ): NetworkResults<LoginUserResponse> {
        val phoneBody = phone.toStringRequestBody()
        val langBody = lang.toStringRequestBody()
        val fullNameBody = full_name.toStringRequestBody()
        val latBody = lat.toStringRequestBody()
        val lonBody = lon.toStringRequestBody()
        val playerIdBody = playerId.toStringRequestBody()
        val detailedAddressBody = detailed_address.toStringRequestBody()
        val cityBody = city.toStringRequestBody()
        val areaBody = area.toStringRequestBody()
        val sectorBody = sector.toStringRequestBody()
        val emailBody = email.toStringRequestBody()
        val addressLineBody = address_line1.toStringRequestBody()
        try {
            val result = ApiClient.retrofitService.addUser(
                lang = langBody,
                phone = phoneBody,
                full_name = fullNameBody,
                lat = latBody,
                lon = lonBody,
                playerId = playerIdBody,
                detailed_address = detailedAddressBody,
                city = cityBody,
                area = areaBody,
                sector = sectorBody,
                email = emailBody,
                addressLineBody
            )
            return if (result.isSuccessful) {
                NetworkResults.Success(result.body()!!)
            } else {
                val errorBody = result.errorBody()?.string()
                errorBody?.let {
                    e("Repository Error Message", it)
                    try {
                        // Convert the error response JSON to a common Error Model
                        val apiResponse: MessageResponse =
                            Gson().fromJson(it, MessageResponse::class.java)
                        NetworkResults.ErrorMessage(apiResponse)
                    } catch (e: JsonSyntaxException) {
                        // Handle the case where the error response is not a valid JSON
                        NetworkResults.Error(e)
                    }
                } ?: NetworkResults.Error(Exception("Error body is null"))
            }
        } catch (e: Exception) {
            return NetworkResults.Error(e)
        }
    }


    suspend fun getBestSelling(
        uid: String,
        lang: String
    ): NetworkResults<BestSellingResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val langBody = lang.toStringRequestBody()
            try {
                val results = ApiClient.retrofitService.getBestSelling(
                    uidBody,
                    langBody
                )
                NetworkResults.Success(results)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun setDefaultAddress(
        uid: String,
        profile_id: String,
        as_default: String
    ): NetworkResults<MessageResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val profileIdBody = profile_id.toStringRequestBody()
            val asDefaultBody = as_default.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.setDefaultAddress(
                    uidBody,
                    profileIdBody,
                    asDefaultBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun deleteAddress(
        uid: String,
        profile_id: String
    ): NetworkResults<MessageResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val profileIdBody = profile_id.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.deleteAddress(
                    uidBody,
                    profileIdBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun addAddress(
        uid: String,
        address_line1: String,
        detailed_address: String,
        lat: String,
        lon: String,
        city_and_area: String,
    ): NetworkResults<MessageResponse> {
        val uidBody = uid.toStringRequestBody()
        val addressLineBody = address_line1.toStringRequestBody()
        val detailedAddressBody = detailed_address.toStringRequestBody()
        val latBody = lat.toStringRequestBody()
        val lonBody = lon.toStringRequestBody()
        val cityAndAreaBody = city_and_area.toStringRequestBody()

        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.addAddress(
                    uid = uidBody,
                    address_line1 = addressLineBody,
                    detailed_address = detailedAddressBody,
                    lat = latBody,
                    lon = lonBody,
                    city_and_area = cityAndAreaBody,
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }

        }
    }

    suspend fun aboutUs(): NetworkResults<AboutUsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.aboutUs()
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun privacyPolicy(): NetworkResults<PrivacyPolicyResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.privacyPolicy()
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun termsAndConditions(): NetworkResults<PrivacyPolicyResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.termsAndConditions()
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getWarranties(uid: String, lang: String): NetworkResults<WarrantiesResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val langBody = lang.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.getWarranties(uidBody, langBody)
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun viewOrders(
        uid: String,
        order_state: String,
        lang: String
    ): NetworkResults<ViewOrdersResponse> {
        val uidBody = uid.toStringRequestBody()
        val orderStateBody = order_state.toStringRequestBody()
        val langBody = lang.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.viewOrders(uidBody, orderStateBody, langBody)
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun employeeLogin(
        userName: String,
        password: String,
        playerId: String,
        lang: String
    ): NetworkResults<EmployeeLoginResponse> {
        val userNameBody = userName.toStringRequestBody()
        val passwordBody = password.toStringRequestBody()
        val playerIdBody = playerId.toStringRequestBody()
        val langBody = lang.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.employeeLogin(
                    userNameBody,
                    passwordBody,
                    playerIdBody,
                    langBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun viewShopperOrders(
        uid: String,
        order_state: String,
        lang: String
    ): NetworkResults<ShopperOrdersResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val orderStateBody = order_state.toStringRequestBody()
            val langBody = lang.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.viewShopperOrders(
                    uidBody,
                    orderStateBody,
                    langBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun startEndEmployeeOrder(
        uid: String,
        order_id: String,
        start_flag: String,
        type_flag: String,
        lang: String
    ): NetworkResults<MessageResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val orderIdBody = order_id.toStringRequestBody()
            val startFlagBody = start_flag.toStringRequestBody()
            val typeFlagBody = type_flag.toStringRequestBody()
            val langBody = lang.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.startEndEmployeeOrder(
                    uidBody,
                    orderIdBody,
                    startFlagBody,
                    typeFlagBody,
                    langBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun setShopperOrder(
        uid: String,
        order_id: String,
        order_item_id: String,
        obtainable: String,
        sku: String,
        lang: String,
    ): NetworkResults<MessageResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val orderIdBody = order_id.toStringRequestBody()
            val orderItemIdBody = order_item_id.toStringRequestBody()
            val obtainableBody = obtainable.toStringRequestBody()
            val skuBody = sku.toStringRequestBody()
            val langBody = lang.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.setShopperOrder(
                    uidBody,
                    orderIdBody,
                    orderItemIdBody,
                    obtainableBody,
                    skuBody,
                    langBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun viewShopperOrderProducts(
        uid: String,
        order_id: String,
        item_state: String,
        lang: String
    ): NetworkResults<ShopperOrderProducts> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val orderIdBody = order_id.toStringRequestBody()
            val itemStateBody = item_state.toStringRequestBody()
            val langBody = lang.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.viewShopperOrderProducts(
                    uidBody,
                    orderIdBody,
                    itemStateBody,
                    langBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun viewDriverOrders(
        uid: String,
        order_state: String,
        lang: String
    ): NetworkResults<DriverOrdersResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val orderStateBody = order_state.toStringRequestBody()
            val langBody = lang.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.viewDriverOrders(
                    uidBody, orderStateBody, langBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun viewDriverOrderDetails(
        uid: String,
        order_id: String,
        lang: String
    ): NetworkResults<DriverOrderDetailsResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val orderIdBody = order_id.toStringRequestBody()
            val langBody = lang.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.viewDriverOrderDetails(
                    uidBody,
                    orderIdBody,
                    langBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getAddressDetails(
        profile_id: String
    ): NetworkResults<GetAddressDetailsResponse> {
        return withContext(Dispatchers.IO) {
            val profileIdBody = profile_id.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.getAddressDetails(
                    profileIdBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun editAddress(
        uid: String,
        profile_id: String,
        city_and_area: String,
        address_line1: String,
        detailed_address: String,
        lat: String,
        lon: String,
    ): NetworkResults<MessageResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val profileIdBody = profile_id.toStringRequestBody()
            val cityAndAreaBody = city_and_area.toStringRequestBody()
            val addressLine1Body = address_line1.toStringRequestBody()
            val detailedAddressBody = detailed_address.toStringRequestBody()
            val latBody = lat.toStringRequestBody()
            val lonBody = lon.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.editAddress(
                    uidBody,
                    profileIdBody,
                    cityAndAreaBody,
                    addressLine1Body,
                    detailedAddressBody,
                    latBody,
                    lonBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun updateUser(
        uid: String,
        full_name: String,
        email: String,
    ): NetworkResults<MessageResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val fullNameBody = full_name.toStringRequestBody()
            val emailBody = email.toStringRequestBody()

            try {
                val result = ApiClient.retrofitService.updateUser(
                    uidBody,
                    fullNameBody,
                    emailBody,
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun getOfferProducts(
        uid: String,
        lang: String
    ): NetworkResults<GetOfferProductsResponse> {
        return withContext(Dispatchers.IO) {
            val uidBody = uid.toStringRequestBody()
            val langBody = lang.toStringRequestBody()
            try {
                val result = ApiClient.retrofitService.getOfferProducts(uidBody, langBody)
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun viewOrderDetails(
        uid: String,
        order_id: String
    ): NetworkResults<ViewOrderDetailsResponse> {
        val uidBody = uid.toStringRequestBody()
        val orderIdBody = order_id.toStringRequestBody()
        return withContext(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.viewOrderDetails(
                    uidBody,
                    orderIdBody
                )
                NetworkResults.Success(result)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }

    suspend fun reOrder(
        uid: String,
        order_id: String
    ): NetworkResults<ReOrderResponse> {
        val uidBody = uid.toStringRequestBody()
        val orderIdBody = order_id.toStringRequestBody()

        return withContext(Dispatchers.IO) {
            try {
                val results = ApiClient.retrofitService.reOrder(
                    uidBody, orderIdBody
                )
                NetworkResults.Success(results)
            } catch (e: Exception) {
                NetworkResults.Error(e)
            }
        }
    }
}