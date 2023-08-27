package com.codebrew.whrzat.ui.feed.happening

import android.util.Log
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.feed.HappeningListData
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedHappeningPresenter :FeedHappeningContract.Presenter{
    private var view: FeedHappeningContract.View? = null

    override fun feedHappeningApi(page: Int,userId: String,radius:Int,lat:String,lng:String) {
        Log.e("params",page.toString()+" "+userId+" "+radius+" "+lat+" "+lng)

        RetrofitClient.get().apiGetHappeningList(page,20,userId,radius,lat,lng).enqueue(object : Callback<HappeningListData> {
            override fun onResponse(call: Call<HappeningListData>, response: Response<HappeningListData>) {

                if (response.isSuccessful) {
                    if (response.body()?.data?.imageData?.isNotEmpty()!!) {
                        response.body()?.data?.let {
                            view?.successFeedHappeningApi(it)
                        }
                    }else{
                        view?.noEventFound()
                    }
                } else {
                    Log.d("11ApiCallResponse","Fail")
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {

                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(response.errorBody()!!)}
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<HappeningListData>, t: Throwable) {

                Log.d("11 ApiCallResponse==","Fail"+t.message)
               // view?.failureApi()
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
                        view?.sessionExpire()
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
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.failureApi()
                view?.dismissLoading()
            }
        })
    }
    override fun attachView(view: FeedHappeningContract.View) {
        this.view = view
    }
    override fun detachView() {
        view = null
    }


}