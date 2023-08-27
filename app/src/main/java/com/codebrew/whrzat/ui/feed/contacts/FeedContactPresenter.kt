package com.codebrew.whrzat.ui.feed.contacts

import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.ApiContacts
import com.codebrew.whrzat.webservice.pojo.feed.Feed
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FeedContactPresenter : FeedContactContract.Presenter {

    private var view: FeedContactContract.View? = null

    override fun feedApi(userId: String, isLoader: Boolean) {
        if (isLoader) {
            view?.showLoading()
        }
        RetrofitClient.get().apiGetFeeds(userId).enqueue(object : Callback<Feed> {
            override fun onResponse(call: Call<Feed>, response: Response<Feed>) {

                if (response.isSuccessful) {
                    response.body()?.data?.let { view?.successFeedApi(it) }
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<Feed>, t: Throwable) {
                view?.failureApi()
                view?.dismissLoading()
            }
        })
    }

    override fun love(userId: String, imageId: String, isLike: Boolean) {
        var isLikeString = ""
        /*  if(isLike){
              isLikeString="true"
          }else{
              isLikeString="false"
          }*/
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

    override fun apiReport(imageId: String,userId:String) {
        view?.showLoading()
        RetrofitClient.get().apiReport(imageId,userId).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.successReport()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }
                view?.dismissLoading()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.failureApi()
            }
        })
    }

    override fun apiSyncContacts(contactsObj: ApiContacts) {
       // view?.showLoading()
        RetrofitClient.get().apiSynContacts(contactsObj).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.apiSuccessContacts()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }
                view?.dismissLoading()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.failureApi()
            }
        })
    }


    override fun detachView() {
        view = null
    }

    override fun attachView(view: FeedContactContract.View) {
            this.view = view
    }

}