package com.codebrew.whrzat.ui.chat.userchat

import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.chat.ChatData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatPresenter : ChatContract.Presenter {

    private var view: ChatContract.View? = null


    override fun receiveChatApi(userId: String, otherUserId: String) {
        view?.showLoading()
        RetrofitClient.get().apiChatHistory(userId, otherUserId, 0, 0).enqueue(object : Callback<ChatData> {

            override fun onResponse(call: Call<ChatData>, response: Response<ChatData>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        view?.successChatReceiveApi(it)
                    }

                } else {
                    if (response.code() == ApiConstants.USER_DELETED) {
                        view?.userDeleted()
                    }else if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let {
                            view?.errorApi(it)
                        }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<ChatData>, t: Throwable) {
                view?.failureApi()
                view?.dismissLoading()

            }
        })
    }

    override fun attachView(view: ChatContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}