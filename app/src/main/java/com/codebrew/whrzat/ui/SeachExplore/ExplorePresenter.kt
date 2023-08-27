package com.codebrew.whrzat.ui.SeachExplore

import android.util.Log
import com.codebrew.whrzat.ui.Home.HomeContract
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.explore.EventListData
import com.codebrew.whrzat.webservice.pojo.explore.ExploreData
import com.codebrew.whrzat.webservice.pojo.explore.SendExploreData
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExplorePresenter :ExploreContracts.Presenter{

    private var view: ExploreContracts.View? = null
    override fun performExploreApi(sendExploreData: SendExploreData, lisLoader: Boolean, i: Int, page: Int) {
        val map = HashMap<String, String>()
        map.put(ApiConstants.LONGITUDE, sendExploreData.lng)
        map.put(ApiConstants.LATITUDE, sendExploreData.lat)
        map.put(ApiConstants.USER_ID, sendExploreData.userId)

        if (!sendExploreData.search.isEmpty()) {
            map.put(ApiConstants.SEARCH, sendExploreData.search)
        }

        if (lisLoader) {
            view?.showLoading()
        }

        RetrofitClient.get().apiMapView(map, i, 10, page).enqueue(object : Callback<ExploreData> {
            override fun onResponse(call: Call<ExploreData>, response: Response<ExploreData>) {
                if (response.isSuccessful) {
                    if (response.body()?.data?.mapData?.isNotEmpty()!!) {
                        response.body()?.data?.let { view?.exploreList(it) }
                    } else {
                        view?.noSpotFound()
                    }
                } else {
                    if (response.code() == 401) {
                        view?.sessionExpired()
                    } else if (response.code() == 400) {
                        view?.noSpotFound()
                    } else {
                        if (response.errorBody() != null) {
                            view?.errorExploreApi(response.errorBody()!!)
                        }
                    }
                }

                view?.dismissLoading()
            }

            override fun onFailure(call: Call<ExploreData>, t: Throwable) {
                view?.dismissLoading()
                view?.failureExploreApi()
            }
        })
    }

    override fun mapViewApi(sendExploreData: SendExploreData, lisLoader: Boolean, i: Int) {
        view?.showLoading()
        val map = HashMap<String, String>()
        map.put(ApiConstants.LONGITUDE, sendExploreData.lng)
        map.put(ApiConstants.LATITUDE, sendExploreData.lat)
        map.put(ApiConstants.USER_ID, sendExploreData.userId)

        if (!sendExploreData.search.isEmpty()) {
            map.put(ApiConstants.SEARCH, sendExploreData.search)
        }
        RetrofitClient.get().apiMapView(map, i, 0, 0).enqueue(object : Callback<ExploreData> {
            override fun onResponse(call: Call<ExploreData>, response: Response<ExploreData>) {
                if (response.isSuccessful) {
                    if (response.body()?.data?.mapData?.isNotEmpty()!!) {
                        response.body()?.data?.let { view?.exploreList(it) }
                    } else {
                        view?.dismissLoading()
                    }
                } else {
                    view?.dismissLoading()
                    if (response.code() == 401) {
                        view?.sessionExpired()
                    } else if (response.code() == 400) {
                        view?.noSpotFound()
                    } else {
                        if (response.errorBody() != null) {
                            Log.d("ApiCallResponse", "Fail" + response.errorBody()!!)
                            view?.errorExploreApi(response.errorBody()!!)
                        }
                    }

                }

                view?.dismissLoading()
            }

            override fun onFailure(call: Call<ExploreData>, t: Throwable) {
                Log.d("ApiCallResponse", "Fail" + t?.message)
                view?.dismissLoading()
                view?.failureExploreApi()
            }
        })
    }

    override fun performEventListApi(sendExploreData: SendExploreData, lisLoader: Boolean, i: Int, page: Int) {
        val map = HashMap<String, String>()
        map.put(ApiConstants.LONGITUDE, sendExploreData.lng)
        map.put(ApiConstants.LATITUDE, sendExploreData.lat)
        map.put(ApiConstants.USER_ID, sendExploreData.userId)
        if (lisLoader) {
            view?.showLoading()
        }
        if (!sendExploreData.search.isEmpty()) {
            map.put(ApiConstants.SEARCH, sendExploreData.search)
        }
        Log.d("ApiCall", "PerformEventList")
        RetrofitClient.get().apiGetEventList(map, 25, page, i).enqueue(object : Callback<EventListData> {
            override fun onResponse(call: Call<EventListData>, response: Response<EventListData>) {

                if (response.isSuccessful) {
                   // Log.d("ApiCallResponse", (response.body()))
                    if (response.body()?.data?.events?.isNotEmpty()!!) {
                        response.body()?.data?.let { view?.eventListSuccess(it) }
                    } else {
                        view?.noEventFound()
                    }
                } else {
                    Log.d("ApiCallResponse", "Fail")
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {

                        view?.sessionExpired()
                    } else {
                        response.errorBody()?.let { view?.errorExploreApi(response.errorBody()!!) }
                    }
                }
                view?.dismissLoading()
            }

            override fun onFailure(call: Call<EventListData>, t: Throwable) {
                Log.d("ApiCallResponse", "Fail" + t?.message)
                view?.failureExploreApi()
                view?.dismissLoading()
            }
        })
    }

    override fun performSearchApi(keyword: String) {

    }

    override fun attachView(view: ExploreContracts.View) {
        this.view=view
    }

    override fun detachView() {
        this.view=null
    }

}