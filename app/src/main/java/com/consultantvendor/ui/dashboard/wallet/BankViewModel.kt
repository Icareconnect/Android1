package com.consultantvendor.ui.dashboard.wallet

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

class BankViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val addBank by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val bankAccounts by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val payout by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun addBank(hashMap: HashMap<String, String>) {
        addBank.value = Resource.loading()

        webService.addBank(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            addBank.value = Resource.success(response.body()?.data)
                        } else {
                            addBank.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        addBank.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun bankAccounts(hashMap: HashMap<String, String>) {
        bankAccounts.value = Resource.loading()

        webService.bankAccounts(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        bankAccounts.value = Resource.success(response.body()?.data)
                    } else {
                        bankAccounts.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    bankAccounts.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }

    fun payouts(hashMap: HashMap<String, String>) {
        payout.value = Resource.loading()

        webService.payouts(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            payout.value = Resource.success(response.body()?.data)
                        } else {
                            payout.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        payout.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }
}