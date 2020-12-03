package com.consultantvendor.data.apis

import com.consultantvendor.data.models.requests.UpdateDocument
import com.consultantvendor.data.models.requests.UpdateServices
import com.consultantvendor.data.models.responses.CommonDataModel
import com.consultantvendor.data.models.responses.Revenue
import com.consultantvendor.data.models.responses.UserData
import com.consultantvendor.data.models.responses.appdetails.AppVersion
import com.consultantvendor.data.models.responses.directions.Direction
import com.consultantvendor.data.network.responseUtil.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface WebService {
    companion object {

        private const val LOGIN = "/api/login"
        private const val APP_VERSION = "/api/appversion"
        private const val CLIENT_DETAILS = "/api/clientdetail"
        private const val COUNTRY_DATA = "/api/countrydata"
        private const val PREFERENCES = "/api/master/preferences"
        private const val UPDATE_NUMBER = "/api/update-phone"
        private const val VERIFY_OTP = "/api/verify-otp"
        private const val RESEND_OTP = "api/resend-otp"
        private const val REGISTER = "/api/register"
        private const val FORGOT_PASSWORD = "/api/forgot_password"
        private const val CHANGE_PASSWORD = "/api/password-change"
        private const val PROFILE_UPDATE = "/api/profile-update"
        private const val LOGOUT = "/api/app_logout"
        private const val SEND_SMS = "/api/send-sms"
        private const val UPDATE_FCM_ID = "/api/update-fcm-id"
        private const val ACCEPT_REQUEST = "/api/accept-request"
        private const val START_REQUEST = "/api/start-request"
        private const val MAKE_CALL = "/api/make-call"
        private const val COMPLETE_CHAT = "/api/complete-chat"
        private const val ADD_BANK = "/api/add-bank"
        private const val UPLOAD_IMAGE = "/api/upload-image"
        private const val FEEDS = "/api/feeds"
        private const val VIEW_FEEDS = "/api/feeds/view/{feed_id}"
        private const val ADD_FAVORITE = "/api/feeds/add-favorite/{feed_id}"

        private const val SUBSCRIBE_SERVICE = "/api/subscribe-service"
        private const val REQUESTS = "/api/requests"
        private const val REQUEST_DETAIL="/api/request-detail"
        private const val WALLET_HISTORY = "/api/wallet-history-sp"
        private const val WALLET = "/api/wallet-sp"
        private const val CHAT_LISTING = "/api/chat-listing"
        private const val CHAT_MESSAGES = "/api/chat-messages"
        private const val BANK_ACCOUNTS = "/api/bank-accounts"
        private const val REVENUE = "/api/revenue"
        private const val NOTIFICATIONS = "/api/notifications"
        private const val DOCTOR_DETAIL = "/api/doctor-detail"

        private const val CATEGORIES = "/api/categories"
        private const val GET_SLOTS = "/api/get-slots"

        private const val ADD_CLASS = "/api/add-class"
        private const val CLASSES = "/api/classes"
        private const val CLASS_STATUS = "/api/class/status"
        private const val SERVICES = "/api/services"
        private const val GET_FILTERS = "/api/get-filters"
        private const val UPDATE_SERVICES = "/api/update-services"
        private const val CANCEL_REQUEST = "/api/cancel-request"
        private const val CALL_STATUS = "/api/call-status"
        private const val PAGES = "/api/pages"
        private const val ADD_CARD = "api/add-card"
        private const val UPDATE_CARD = "/api/update-card"
        private const val DELETE_CARD = "/api/delete-card"
        private const val ADD_MONEY = "/api/add-money"
        private const val CARD_LISTING = "/api/cards"
        private const val ORDER_CREATE = "/api/order/create"
        private const val ADDITIONAL_DETAILS="/api/additional-details"
        private const val ADDITIONAL_DETAIL_DATA="/api/additional-detail-data"
        private const val PAYOUTS = "/api/payouts"

        private const val WORKING_HOURS = "/api/workingHours"
        private const val SPEAKOUT_LIST = "/common/listSpeakouts"
        private const val DIRECTIONS="https://maps.googleapis.com/maps/api/directions/json"

    }

    /*POST APIS*/
    @FormUrlEncoded
    @POST(LOGIN)
    fun login(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(APP_VERSION)
    fun appVersion(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<AppVersion>>

    @FormUrlEncoded
    @POST(UPDATE_NUMBER)
    fun updateNumber(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(VERIFY_OTP)
    fun verifyOtp(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(RESEND_OTP)
    fun resendOtp(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @Multipart
    @POST(REGISTER)
    fun register(@PartMap map: HashMap<String, RequestBody>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(FORGOT_PASSWORD)
    fun forgotPassword(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(CHANGE_PASSWORD)
    fun changePassword(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>


    @Multipart
    @POST(PROFILE_UPDATE)
    fun updateProfile(@PartMap map: HashMap<String, RequestBody>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(SEND_SMS)
    fun sendSMS(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @POST(UPDATE_SERVICES)
    fun updateServices(@Body updateServices: UpdateServices): Call<ApiResponse<UserData>>

    @POST(LOGOUT)
    fun logout(): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(SUBSCRIBE_SERVICE)
    fun subscribeService(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(UPDATE_FCM_ID)
    fun updateFcmId(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(ACCEPT_REQUEST)
    fun acceptRequest(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(START_REQUEST)
    fun startRequest(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(MAKE_CALL)
    fun makeCall(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(COMPLETE_CHAT)
    fun completeChat(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @Multipart
    @POST(UPLOAD_IMAGE)
    fun uploadFile(
            @Part("type") typeRequest: RequestBody,
            @Part file: Array<MultipartBody.Part?>
    ): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(ADD_BANK)
    fun addBank(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(FEEDS)
    fun feeds(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(ADD_CLASS)
    fun addClass(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(CLASS_STATUS)
    fun classStatus(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(CANCEL_REQUEST)
    fun cancelRequest(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(CALL_STATUS)
    fun callStatus(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(ADD_CARD)
    fun addCard(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(UPDATE_CARD)
    fun updateCard(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(DELETE_CARD)
    fun deleteCard(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(ADD_MONEY)
    fun addMoney(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(ORDER_CREATE)
    fun orderCreate(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(PAYOUTS)
    fun payouts(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @POST(ADDITIONAL_DETAIL_DATA)
    fun additionalDetailsUpdate(@Body updateDocument: UpdateDocument): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(ADD_FAVORITE)
    fun addFavorite(@Path("feed_id") feed_id: String,
                    @FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>



    /*GET*/

    @GET(CLIENT_DETAILS)
    fun clientDetails(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<AppVersion>>

    @GET(COUNTRY_DATA)
    fun countryData(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(PREFERENCES)
    fun preferences(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>


    @GET(REQUESTS)
    fun request(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>


    @GET(REQUEST_DETAIL)
    fun requestDetail(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(DOCTOR_DETAIL)
    fun doctorDetails(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(WALLET_HISTORY)
    fun walletHistory(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(WALLET)
    fun wallet(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(CHAT_LISTING)
    fun getChatListing(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(CHAT_MESSAGES)
    fun getChatMessage(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(BANK_ACCOUNTS)
    fun bankAccounts(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(REVENUE)
    fun revenue(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<Revenue>>

    @GET(NOTIFICATIONS)
    fun notifications(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(FEEDS)
    fun getFeeds(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(VIEW_FEEDS)
    fun viewFeeds(@Path("feed_id") feed_id: String): Call<ApiResponse<CommonDataModel>>


    @GET(CATEGORIES)
    fun categories(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(GET_SLOTS)
    fun getSlots(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(SERVICES)
    fun services(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(GET_FILTERS)
    fun getFilters(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(CLASSES)
    fun classesList(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(PAGES)
    fun getPages(): Call<ApiResponse<CommonDataModel>>

    @GET(CARD_LISTING)
    fun cardListing(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(ADDITIONAL_DETAILS)
    fun additionalDetails(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>


    @GET(DIRECTIONS)
    fun directions(@QueryMap hashMap: Map<String, String>): Call<Direction>


    /*PUT API*/
    @FormUrlEncoded
    @PUT(WORKING_HOURS)
    fun workingHours(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<Any>>

}