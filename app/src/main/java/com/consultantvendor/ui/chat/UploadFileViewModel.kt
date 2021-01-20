package com.consultantvendor.ui.chat

import androidx.lifecycle.ViewModel
import com.consultantvendor.data.apis.WebService
import com.consultantvendor.data.models.responses.CommonDataModel
import com.consultantvendor.data.network.responseUtil.ApiResponse
import com.consultantvendor.data.network.responseUtil.ApiUtils
import com.consultantvendor.data.network.responseUtil.Resource
import com.consultantvendor.di.SingleLiveEvent
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class UploadFileViewModel @Inject constructor(private val webService: WebService): ViewModel() {

    val uploadFile by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun uploadFile(hashMap: HashMap<String, RequestBody>) {
        uploadFile.value = Resource.loading()

        webService.uploadFile(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        uploadFile.value = Resource.success(response.body()?.data)
                    } else {
                        uploadFile.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    uploadFile.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }
}