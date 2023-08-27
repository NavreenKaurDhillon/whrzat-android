package com.codebrew.whrzat.ui.otherprofile

import android.util.Log
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import com.codebrew.whrzat.webservice.pojo.otherprofile.ProfileData
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilePresenter : ProfileContract.Presenter{

    private var view:ProfileContract.View?=null

    override fun apiBlockProfile(userId: String, blockUserId: String,isBlock:Boolean) {
        view?.showLoading()
        RetrofitClient.get().apiBlockUser(userId,blockUserId,isBlock).enqueue(object:Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(response.isSuccessful){
                    view?.successBlockApi()

                }else{
                    if (response.code() == 401) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.failureApi()
                view?.dismissLoading()
            }
        })
    }

    override fun love(userId: String, imageId: String, isLike: Boolean,likeCount:String,pos:Int) {
        RetrofitClient.get().apiLikeImage(userId, imageId, isLike).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.successLoveApi(likeCount,pos)
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

    override fun apiGetProfile(profileId: String, userId: String) {
        view?.showLoading()
        RetrofitClient.get().apiGetProfile(profileId,userId).enqueue(object:Callback<ProfileData>{
            override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                if(response.isSuccessful){
                   // Log.e("Other profile",(response.body()))
                    response.body()?.data?.let { view?.successProfileApi(it) }

                }else{
                    if (response.code() == 401) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<ProfileData>, t: Throwable) {
                view?.failureApi()
                view?.dismissLoading()
            }
        })

    }

    override fun apiReport(imageId: String,userId: String) {
        view?.showLoading()
        RetrofitClient.get().apiReport(imageId,userId).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.successReportApi()
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

    override fun attachView(view: ProfileContract.View) {
        this.view=view
    }

    override fun detachView() {
        view=null
    }

}