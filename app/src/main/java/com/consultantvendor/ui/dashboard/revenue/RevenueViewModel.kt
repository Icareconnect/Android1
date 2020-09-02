package com.consultantvendor.ui.dashboard.revenue

import androidx.lifecycle.ViewModel
import com.consultantvendor.data.apis.WebService
import com.consultantvendor.data.models.responses.Revenue
import com.consultantvendor.data.network.responseUtil.ApiResponse
import com.consultantvendor.data.network.responseUtil.ApiUtils
import com.consultantvendor.data.network.responseUtil.Resource
import com.consultantvendor.di.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class RevenueViewModel @Inject constructor(private val webService: WebService): ViewModel() {

    val revenue by lazy { SingleLiveEvent<Resource<Revenue>>() }


    fun revenue(hashmap: HashMap<String, String>) {
        revenue.value = Resource.loading()
        webService.revenue(hashmap)
                .enqueue(object : Callback<ApiResponse<Revenue>> {
                    override fun onFailure(
                        call: Call<ApiResponse<Revenue>>,
                        throwable: Throwable) {
                        revenue.value = Resource.error(ApiUtils.failure(throwable))
                    }

                    override fun onResponse(
                        call: Call<ApiResponse<Revenue>>,
                        response: Response<ApiResponse<Revenue>>) {
                        if (response.isSuccessful) {
                            revenue.value = Resource.success(response.body()?.data)
                        } else {
                            revenue.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }
                })
    }

}