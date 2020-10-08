package com.consultantvendor.ui.dashboard.home

import androidx.lifecycle.ViewModel
import com.consultantvendor.data.apis.WebService
import com.consultantvendor.data.models.responses.CommonDataModel
import com.consultantvendor.data.network.responseUtil.ApiResponse
import com.consultantvendor.data.network.responseUtil.ApiUtils
import com.consultantvendor.data.network.responseUtil.Resource
import com.consultantvendor.di.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AppointmentViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val pendingRequest by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val requestDetail by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val callStatus by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val acceptRequest by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val startRequest by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val completeChat by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val cancelRequest by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun request(hashMap: HashMap<String, String>) {
        pendingRequest.value = Resource.loading()

        webService.request(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            pendingRequest.value = Resource.success(response.body()?.data)
                        } else {
                            pendingRequest.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        pendingRequest.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun requestDetail(hashMap: HashMap<String, String>) {
        requestDetail.value = Resource.loading()

        webService.requestDetail(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            requestDetail.value = Resource.success(response.body()?.data)
                        } else {
                            requestDetail.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        requestDetail.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun callStatus(hashMap: HashMap<String, Any>) {
        callStatus.value = Resource.loading()

        webService.callStatus(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            callStatus.value = Resource.success(response.body()?.data)
                        } else {
                            callStatus.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        callStatus.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun acceptRequest(hashMap: HashMap<String, Any>) {
        acceptRequest.value = Resource.loading()

        webService.acceptRequest(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            acceptRequest.value = Resource.success(response.body()?.data)
                        } else {
                            acceptRequest.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        acceptRequest.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun startRequest(hashMap: HashMap<String, Any>) {
        startRequest.value = Resource.loading()

        webService.startRequest(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        startRequest.value = Resource.success(response.body()?.data)
                    } else {
                        startRequest.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    startRequest.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }

    fun completeChat(hashMap: HashMap<String, Any>) {
        completeChat.value = Resource.loading()

        webService.completeChat(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            completeChat.value = Resource.success(response.body()?.data)
                        } else {
                            completeChat.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        completeChat.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun cancelRequest(hashMap: HashMap<String, String>) {
        cancelRequest.value = Resource.loading()

        webService.cancelRequest(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        cancelRequest.value = Resource.success(response.body()?.data)
                    } else {
                        cancelRequest.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    cancelRequest.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }
}