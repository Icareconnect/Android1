package com.consultantvendor.ui.loginSignUp.availability

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

class GetSlotsViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val getSlots by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun getSlots(hashMap: HashMap<String, String>) {
        getSlots.value = Resource.loading()

        webService.getSlots(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            getSlots.value = Resource.success(response.body()?.data)
                        } else {
                            getSlots.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        getSlots.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }
}