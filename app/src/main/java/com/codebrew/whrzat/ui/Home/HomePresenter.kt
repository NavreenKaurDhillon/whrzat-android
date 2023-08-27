package com.codebrew.whrzat.ui.Home

import android.util.Log
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.ApiContacts
import com.codebrew.whrzat.webservice.pojo.explore.EventListData
import com.codebrew.whrzat.webservice.pojo.explore.ExploreData
import com.codebrew.whrzat.webservice.pojo.explore.SendExploreData
import com.codebrew.whrzat.webservice.pojo.feed.Feed
import com.codebrew.whrzat.webservice.pojo.feed.HappeningListData
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomePresenter : HomeContract.Presenter {


    private var view: HomeContract.View? = null

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
               // view?.failureExploreApi()
                view?.dismissLoading()
            }
        })
    }
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
                        response.body()?.data?.let { view?.mapData(it) }
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
           //     view?.failureExploreApi()
            }
        })
    }

    override fun feedHappeningApi(page: Int,userId: String,radius:Int,lat:String,lng:String) {
        Log.e("params",page.toString()+" "+userId+" "+radius+" "+lat+" "+lng)
           view?.showLoading()
        RetrofitClient.get().apiGetHappeningList(page,20,userId,radius,lat,lng).enqueue(object : Callback<HappeningListData> {
            override fun onResponse(call: Call<HappeningListData>, response: Response<HappeningListData>) {

                if (response.isSuccessful) {
                    if (response.body()?.data?.imageData?.isNotEmpty()!!) {
                        response.body()?.data?.let {
                            view?.successFeedHappeningApi(it)
                        }
                    }else{
                        view?.failureApi("No recent images from nearby")
                    }
                } else {
                    Log.d("11ApiCallResponse","Fail")
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {

                        view?.sessionExpired()
                    } else {
                        response.errorBody()?.let { view?.errorApi(response.errorBody()!!)}
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<HappeningListData>, t: Throwable) {
                Log.e("11 ApiCallResponse==","Fail"+t?.message)
                 view?.failureApi(t?.message)
                view?.dismissLoading()
            }
        })
    }

    override fun apiReport(imageId: String, userId: String) {
        view?.showLoading()
        RetrofitClient.get().apiReport(imageId,userId).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.successReport()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpired()
                    } else {
                        response.errorBody()?.let { view?.errorReportApi(it) }
                    }
                }
                view?.dismissLoading()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.failureReportApi()
            }
        })
    }

    override fun love(userId: String, imageId: String, isLike: Boolean) {
        RetrofitClient.get().apiLikeImage(userId, imageId, isLike).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.successLoveApi()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpired()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.failureApi(t?.message)
                view?.dismissLoading()
            }
        })
    }

    override fun feedApi(userId: String, isLoader: Boolean) {
        if (isLoader) {
            view?.showLoading()
        }
        Log.e("params","customerId "+userId)
        RetrofitClient.get().apiGetFeeds(userId).enqueue(object : Callback<Feed> {

            override fun onResponse(call: Call<Feed>, response: Response<Feed>) {

                if (response.isSuccessful) {
                    response.body()?.data?.let { view?.successFeedApi(it) }
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpired()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<Feed>, t: Throwable) {
                view?.failureApi(t?.message)
                view?.dismissLoading()
            }
        })
    }

    override fun apiSyncContacts(contactsObj: ApiContacts) {
         view?.showLoading()
        RetrofitClient.get().apiSynContacts(contactsObj).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.apiSuccessContacts()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpired()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }
                view?.dismissLoading()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.failureApi(t?.message)
            }
        })
    }

    override fun performSearchApi(keyword: String) {

    }

    override fun attachView(view: HomeContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}
