package com.codebrew.whrzat.ui.settings.blocked

import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.BlockList
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BlockedPresenter:BlockedContract.Presenter {


    override fun apiBlockList(userId: String) {
        view?.showLoading()
        RetrofitClient.get().apiBlockList(userId).enqueue(object :Callback<BlockList>{
            override fun onResponse(call: Call<BlockList>, response: Response<BlockList>) {
                if(response.isSuccessful){
                    response.body()?.data?.let { view?.successBlockedApi(it) }
                }else{
                    if(response.code()== ApiConstants.UNAUTHORIZED_ACCESS){
                        view?.sessionExpired()
                    }else {
                        response.errorBody()?.let { view?.errorBlockedApi(it) }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<BlockList>, t: Throwable) {
                view?.dismissLoading()
                view?.failureBlockedApi()

            }
        })
    }

    override fun apiUnBlock(userId: String, otherUserId:String) {
        view?.showLoading()
        RetrofitClient.get().apiBlockUser(userId,otherUserId,false).enqueue(object:Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(response.isSuccessful){
                   view?.successUnBlock()

                }else{
                    if (response.code() == 401) {
                        view?.sessionExpired()
                    } else {
                        response.errorBody()?.let { view?.errorBlockedApi(it) }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.failureBlockedApi()
                view?.dismissLoading()
            }
        })
    }

    private var view:BlockedContract.View?=null


    override fun attachView(view:BlockedContract.View) {
        this.view=view
    }

    override fun detachView() {
        view=null
    }
}
