package com.consultantvendor.ui.dashboard.feeds

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

class FeedViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val feeds by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val getFeeds by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val viewFeeds by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val addFavorite by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun feeds(hashMap: HashMap<String, String>) {
        feeds.value = Resource.loading()

        webService.feeds(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            feeds.value = Resource.success(response.body()?.data)
                        } else {
                            feeds.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        feeds.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun getFeeds(hashMap: HashMap<String, String>) {
        getFeeds.value = Resource.loading()

        webService.getFeeds(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            getFeeds.value = Resource.success(response.body()?.data)
                        } else {
                            getFeeds.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        getFeeds.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun viewFeeds(feed_id: String) {
        viewFeeds.value = Resource.loading()

        webService.viewFeeds(feed_id)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            viewFeeds.value = Resource.success(response.body()?.data)
                        } else {
                            viewFeeds.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        viewFeeds.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun addFavorite(feed_id: String,hashMap: HashMap<String, String>) {
        addFavorite.value = Resource.loading()

        webService.addFavorite(feed_id,hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            addFavorite.value = Resource.success(response.body()?.data)
                        } else {
                            addFavorite.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        addFavorite.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }


}