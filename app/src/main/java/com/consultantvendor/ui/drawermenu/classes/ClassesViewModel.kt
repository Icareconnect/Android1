package com.consultantvendor.ui.drawermenu.classes

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

class ClassesViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val addClass by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val classStatus by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val categories by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val classes by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val services by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val getFilters by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }


    fun addClass(hashMap: HashMap<String, String>) {
        addClass.value = Resource.loading()

        webService.addClass(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            addClass.value = Resource.success(response.body()?.data)
                        } else {
                            addClass.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        addClass.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun classStatus(hashMap: HashMap<String, String>) {
        classStatus.value = Resource.loading()

        webService.classStatus(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            classStatus.value = Resource.success(response.body()?.data)
                        } else {
                            classStatus.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        classStatus.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun classesList(hashMap: HashMap<String, String>) {
        classes.value = Resource.loading()

        webService.classesList(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            classes.value = Resource.success(response.body()?.data)
                        } else {
                            classes.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        classes.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun categories(hashMap: HashMap<String, String>) {
        categories.value = Resource.loading()

        webService.categories(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            categories.value = Resource.success(response.body()?.data)
                        } else {
                            categories.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        categories.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun services(hashMap: HashMap<String, String>) {
        services.value = Resource.loading()

        webService.services(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            services.value = Resource.success(response.body()?.data)
                        } else {
                            services.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        services.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun getFilters(hashMap: HashMap<String, String>) {
        getFilters.value = Resource.loading()

        webService.getFilters(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            getFilters.value = Resource.success(response.body()?.data)
                        } else {
                            getFilters.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        getFilters.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }
}