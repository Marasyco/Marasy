package com.blueray.marasy.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

sealed class NetworkResults<out R> {
    data class Success<out T>(val data: T) : NetworkResults<T>()
    data class ErrorMessage<out T>(val data: MessageResponse?) : NetworkResults<T>()
    data class Error(val exception: Exception) : NetworkResults<Nothing>()
}

data class MessageResponse(
    val msg: Msg
)

data class Msg(
    val message: String,
    val status: Int
)

data class UserLoginResponse(
    val `data`: UserLoginData,
    val msg: Msg
)

data class UserLoginData(
    val detailed_address: String,
    val full_name: String,
    val id: String,
    val lat: String,
    val lon: String,
    val mail: String,
    val phone: String,
    val role: String
)

data class HomeSliderResponse(
    val `data`: List<HomeSliderData>,
    val msg: Msg
)

data class HomeSliderData(
    val body: String,
    val brand_id: String,
    val category_id: String,
    val images: String,
    val product_id: String,
    val slide_id: String,
    val title: String
)

data class CategoriesResponse(
    val `data`: List<CategoriesData>,
    val msg: Msg
)

data class CategoriesData(
    val id: String,
    val image: String,
    val name: String
)

data class GetSubCategoriesResponse(
    val `data`: List<SubCategoriesData>,
    val msg: Msg
)

data class SubCategoriesData(
    val image: String,
    val name: String,
    val tid: Int,
    val has_childs: Boolean
)

data class ProductDetailsResponse(
    val `data`: ProductDetailsData,
    val msg: Msg
)

data class ProductDetailsData(
    val attributes: List<MainAttribute>,
    val body: String,
    val fav: Int,
    val images: String,
    val new_arrival: String,
    val offerFlag: Int,
    val offer_end_date: String,
    val offer_end_date_timer: OfferEndDateTimer,
    val offer_start_date: String,
    val pid: String,
    val tid: String,
    val title: String,
    val variations: List<Variation>,
    val related: List<Product>,
    val steps_for_use: String? = null,
    val features: String? = null,
    val unknown_price: Int = 0
)

data class Variation(
    val attributes: List<Attribute>,
    val max_quantity: String,
    val min_quantity: String,
    val offerFlag: Int,
    val offer_end_date: String,
    val offer_end_date_timer: OfferEndDateTimer,
    val offer_start_date: String,
    val old_price: String,
    val price: String,
    val sku: String,
    val vid: String
)


data class OfferEndDateTimer(
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val months: Int,
    val seconds: Int,
    val years: Int
)

data class MainAttribute(
    val attribute_id: String,
    val attribute_name: String,
    val options: List<Option>
)

data class Attribute(
    val attribute_id: String,
    val attribute_label: String,
    val attribute_name: String
)

data class Option(
    val attribute_value_id: String,
    val attribute_value_label: String
)

data class GetProductsResponse(
    val `data`: List<Product>,
    val msg: Msg,
    val pager: Pager
)

data class Product(
    val attributes: List<Attribute>,
    val body: String,
    val fav: Int,
    val images: String,
    val new_arrival: String,
    val offerFlag: Int,
    val offer_end_date: String,
    val offer_end_date_timer: OfferEndDateTimer,
    val offer_start_date: String,
    val pid: String,
    val tid: String,
    val title: String,
    val variations: List<Variation>,
    val unknown_price: Int = 0
)

data class Pager(
    val count: Int,
    val limit: Int,
    val page: Int
)

data class GetFavoriteProductsResponse(
    val msg: Msg,
    val `data`: List<Product>
)

data class GetNotificationResponse(
    val `data`: List<String>,
    val data_1: List<NotificationData>,
    val msg: Msg
)

data class NotificationData(
    val title: String,
    val product_id: String?,
    val category_id: String?,
    val brand_id: String?,
    val notification_id: String,
    val body: String,
)


data class ViewCartResponse(
    val `data`: ViewCartData,
    val msg: Msg
)

data class ViewCartData(
    val coupons: List<Any>,
    val discount_amount: String,
    val items: List<CartItem>,
    val order_id: String,
    val shiping_fees: Int,
    val state: String,
    val time_to_deliverd: String,
    val total_order_price: String,
    val total_order_price_after: String
)

data class CartItem(
    val body: String,
    val fav: Int,
    val image: String,
    val item_discount_amount: String,
    val max_quantity: Any,
    val new_arrival: String,
    val offerFlag: Int,
    val offer_end_date: String,
    val offer_end_date_timer: OfferEndDateTimer,
    val offer_start_date: String,
    val old_price: String,
    val order_id: String,
    val order_item_id: String,
    val pid: String,
    val price: String,
    val quantity: Int,
    val tid: String,
    val title: String,
    val total_unit_price: String,
    val vid: String
)

data class GetMyAddressResponse(
    val `data`: List<GetMyAddressData>?,
    val msg: Msg
)

data class GetMyAddressData(
    @SerializedName("FullAddress  ") val FullAddress: FullAddress,
    val is_default: String,
    val profile_id: String
)

data class FullAddress(
    val locality: String,
    val address_line1: String,
    val country_code: String,
    val detailed_address: String,
    val city_and_area_id: String?,
    val city_and_area: String?,
    val lat: Double?,
    val lon: Double?,
)

data class ViewProfileResponse(
    val `data`: ViewProfileData,
    val msg: Msg
)

data class ViewProfileData(
    val address: Address,
    val mail: String,
    val personal: Personal,
    val role: String,
    val uid: String
)

data class Personal(
    val full_name: String,
    val mail: String,
    val phone: String
)

data class Address(
    val area_id: String,
    val area_name: String,
    val city_and_area: String,
    val city_and_area_id: String,
    val city_id: String,
    val city_name: String,
    val detailed_address: String,
    val lat: Double,
    val lon: Double
)

data class GetSectorsResponse(
    val `data`: List<SectorsData>,
    val msg: Msg
)

data class SectorsData(
    val name: String,
    val tid: String
)

data class GetCitiesResponse(
    val `data`: List<GetCitiesData>,
    val msg: Msg
)

data class GetCitiesData(
    val name: String,
    val tid: String
)

data class LoginUserResponse(
    val `data`: LoginUserData,
    val msg: Msg
)

data class LoginUserData(
    val city_and_area: String,
    val detailed_address: String,
    val full_name: String,
    val id: String,
    val lat: String,
    val lon: String,
    val mail: String,
    val phone: String,
    val role: String
)

data class BestSellingResponse(
    val `data`: List<Product>,
    val msg: Msg
)

data class AboutUsResponse(
    val `data`: List<AboutUsData>,
    val msg: Msg
)

data class AboutUsData(
    val body: String,
    val title: String
)

data class PrivacyPolicyResponse(
    val `data`: PrivacyPolicyData,
    val msg: Msg
)

data class PrivacyPolicyData(
    val body: String,
    val title: String
)

data class WarrantiesResponse(
    val `data`: List<WarrantiesData>,
    val msg: Msg
)

data class WarrantiesData(
    val body: String,
    val customer_id: String,
    val invoice_no: String,
    val product_id: String,
    val purchase_date: String,
    val warranty_expiry_date: String,
    val warranty_period_in_month: String,
    val warranty_status: String
)


data class ViewOrdersResponse(
    val `data`: List<ViewOrdersData>,
    val msg: Msg
)

data class ViewOrdersData(
    val billing_information: BillingInformation,
    val can_update: Int,
    val created: String,
    val date: String,
    val driver_id: String,
    val driver_name: String,
    val driver_phone: String,
    val items: List<Item>,
    val order_id: String,
    val order_items_count: Int,
    val order_number: String,
    val order_rating: String,
    val payment_gateway: String,
    val shopper_id: String,
    val shopper_name: String,
    val shopper_phone: String,
    val state: Int,
    val state_name: String,
    val total_order_price: String
)

data class BillingInformation(
    val address_line1: String,
    val city: String,
    val detailed_address: String,
    val is_default: String,
    val lat: Double,
    val lon: Double,
    val profile_id: String
)

data class Item(
    val image: String,
    val order_item_id: String,
    val quantity: Int,
    val title: String,
    val total_unit_price: String,
    val unit_price: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(image)
        parcel.writeString(order_item_id)
        parcel.writeInt(quantity)
        parcel.writeString(title)
        parcel.writeString(total_unit_price)
        parcel.writeString(unit_price)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }
}

data class EmployeeLoginResponse(
    val `data`: EmployeeLoginData,
    val msg: Msg
)

data class EmployeeLoginData(
    val car_plate_number: String,
    val full_name: String,
    val phone: String,
    val player_id: String,
    val role: String,
    val uid: String,
    val user_name: String
)

data class ShopperOrdersResponse(
    val `data`: List<ShopperOrdersData>,
    val msg: Msg
)

data class ShopperOrdersData(
    val billing_information: BillingInformation,
    val date: String,
    val field_full_name: String,
    val customer_mail: String,
    val customer_phone_number: String,
    val immediately: Boolean,
    val items: List<ShopperItem>,
    val notes: String,
    val order_id: String,
    val order_items_count: Int,
    val order_number: String,
    val payment_gateway: String,
    val state: Int,
    val state_name: String,
    val total_order_price: String
)

data class ShopperItem(
    val cartoon_quantity: Int,
    val description: String,
    val image: String,
    val number_of_items_per_carton: Int,
    val order_item_id: String,
    val product_expire_date: String,
    val quantity: Int,
    val title: String,
    val total_unit_price: Double,
    val unit_price: Double,
    val unit_quantity: Int
)

data class ShopperOrderProducts(
    val `data`: OrderProductsData,
    val msg: Msg
)

data class OrderProductsData(
    val assembling_date_deff: AssemblingDateDeff,
    val billing_information: BillingInformation,
    val created: String,
    val deliveirng_date_deff: DeliveirngDateDeff,
    val end_assembling_date: String,
    val end_deliveirng_date: String,
    val immediately: Boolean,
    val items: List<ShopperOrderItem>,
    val notes: String,
    val order_id: String,
    val order_items_count: Int,
    val order_items_not_obtainable_count: Int,
    val order_items_obtainable_count: Int,
    val order_number: String,
    val placed: String,
    val start_assembling_date: String,
    val start_deliveirng_date: String,
    val state: Int,
    val state_name: String
)

data class ShopperOrderItem(
    val description: String,
    val image: String,
    val obtainable: Int,
    val order_item_id: String,
    val product_expire_date: String,
    val quantity: Int,
    val sku: String,
    val title: String,
    val total_unit_price: String,
    val unit_price: String,
    val unit_quantity: Int
)

data class DeliveirngDateDeff(
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val months: Int,
    val seconds: Int,
    val years: Int
)

data class AssemblingDateDeff(
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val months: Int,
    val seconds: Int,
    val years: Int
)

data class DriverOrdersResponse(
    val `data`: List<DriverOrdersData>,
    val msg: Msg
)

data class DriverOrdersData(
    val billing_information: BillingInformation,
    val customer_mail: String,
    val customer_phone_number: String,
    val date: String,
    val field_full_name: String,
    val immediately: Boolean,
    val items: List<Item>,
    val notes: String,
    val order_id: String,
    val order_items_count: Int,
    val order_number: String,
    val payment_gateway: String,
    val show_price_input: String,
    val state: Int,
    val state_name: String,
    val total_order_price: String
)

data class DriverOrderDetailsResponse(
    val `data`: DriverOrderDetailsData,
    val msg: Msg
)

data class DriverOrderDetailsData(
    val allow_rate: Int,
    val billing_information: BillingInformation,
    val created: String,
    val customer_full_name: String,
    val customer_mail: String,
    val customer_phone_number: String,
    val immediately: Boolean,
    val items: List<Item>,
    val notes: String,
    val order_id: String,
    val order_items_count: Int,
    val order_number: String,
    val payment_gateway: String,
    val placed: String,
    val shiping_fees: ShipingFees,
    val show_price_input: String,
    val state: Int,
    val state_name: String,
    val total_order_price: String
)

data class ShipingFees(
    val label: String,
    val amount: String,
    val currencyCode: String,
)

data class GetAddressDetailsResponse(
    val `data`: List<GetAddressDetailsData>,
    val msg: Msg
)

data class GetAddressDetailsData(
    @SerializedName("FullAddress  ") val FullAddress: AddressDetailsFullAddress,
    val is_default: String,
    val profile_id: String
)

data class AddressDetailsFullAddress(
    val address_line1: String,
    val country_code: String,
    val detailed_address: String,
    val lat: Double,
    val locality: String,
    val lon: Double,
    val city_and_area_id: String,
    val city_and_area: String,
    val city_id: String?
)

data class GetOfferProductsResponse(
    val `data`: List<Product>,
    val msg: Msg
)

data class ViewOrderDetailsResponse(
    val `data`: ViewOrderDetailsData,
    val msg: Msg
)

data class ViewOrderDetailsData(
    val allow_rate: Int,
    val assembling_date_deff: AssemblingDateDeff,
    val billing_information: BillingInformation,
    val can_update: Int,
    val created: String,
    val deliveirng_date_deff: String,
    val driver_id: Any,
    val driver_name: Any,
    val driver_phone_number: Any,
    val end_assembling_date: String,
    val end_deliveirng_date: String,
    val expected_deliveiry_date: String,
    val items: List<Item>,
    val order_id: String,
    val order_items_count: Int,
    val order_number: String,
    val order_rating: String,
    val placed: String,
    val shopper_id: Any,
    val shopper_name: Any,
    val shopper_phone_number: Any,
    val start_assembling_date: String,
    val start_deliveirng_date: String,
    val state: Int,
    val state_name: String,
    val total_order_price: String,
    val order_date: String,
    val shiping_fees: ShipingFees
)

data class ReOrderResponse(
    val items_added: Int,
    val message: String,
    val status: Int
)

// Direct array response for trademarks
typealias TrademarksResponse = List<TrademarkData>

data class TrademarkData(
    val id: String,
    val name: String,
    val image: String?
)