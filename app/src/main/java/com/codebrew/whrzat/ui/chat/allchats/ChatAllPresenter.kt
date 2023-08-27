package com.codebrew.whrzat.ui.chat.allchats

import android.util.Log
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.chat.ChatUser
import com.codebrew.whrzat.webservice.pojo.notifications.NotificationMain
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChatAllPresenter : ChatAllContract.Presenter {
    private  val TAG = "ChatAllPresenter"


    private var view: ChatAllContract.View? = null
    override fun chatALlUserApi(userId: String, skip: Int, limit: Int) {
        RetrofitClient.get().apiAllChatuser(userId, 0, 0).enqueue(object : Callback<ChatUser> {
            override fun onResponse(call: Call<ChatUser>, response: Response<ChatUser>) {
             //   Log.d("Message",(response))

                if (response.isSuccessful) {
                    response.body()?.data?.let { view?.successChatAllUserApi(it) }
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<ChatUser>, t: Throwable) {
                view?.failureApi()
                view?.dismissLoading()
                Log.d("MessageError"," error ${t.message}")
            }
        })
    }

    override fun apiDelete(userId: String, id: String) {
        view?.showLoading()
        RetrofitClient.get().apiDelete(userId,id).enqueue(object : Callback<NotificationMain> {
            override fun onResponse(call: Call<NotificationMain>, response: Response<NotificationMain>) {
                if (response.isSuccessful) {
                    view?.successDeleteUser()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<NotificationMain>, t: Throwable) {
                view?.failureApi()
                view?.dismissLoading()
            }
        })
    }


    override fun attachView(view: ChatAllContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}