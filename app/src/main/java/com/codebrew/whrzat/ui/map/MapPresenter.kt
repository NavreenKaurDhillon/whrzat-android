package com.codebrew.whrzat.ui.map

import android.util.Log
import com.codebrew.whrzat.ui.referralCode.ReferralContract
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.explore.ExploreData
import com.codebrew.whrzat.webservice.pojo.explore.SendExploreData
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapPresenter:MapContract.Presenter {
    private var view: MapContract.View? = null
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
                        response.body()?.data?.let { view?.mapData(it) }
                        Log.e("map Activity", " ----------- apiMapView() " + (response.body()))
                    } else {
                        view?.dismissLoading()
                        view?.noSpotAtMapFound()
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

    override fun attachView(view: MapContract.View) {
        this.view = view
    }



    override fun detachView() {
        this.view = null
    }

}