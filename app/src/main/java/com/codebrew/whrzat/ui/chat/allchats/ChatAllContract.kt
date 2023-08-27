package com.codebrew.whrzat.ui.chat.allchats

import com.codebrew.whrzat.webservice.pojo.chat.ChatAllUser
import okhttp3.ResponseBody


class ChatAllContract {

    interface View{

        fun successChatAllUserApi(data: List<ChatAllUser>)

        fun failureApi()

        fun errorApi(errorBody: ResponseBody)

        fun sessionExpire()

        fun showLoading()

        fun dismissLoading()
        fun successDeleteUser()
    }

    interface Presenter{
        fun chatALlUserApi(userId:String,skip:Int,limit:Int)

        fun attachView(view: View)

        fun detachView()
        fun apiDelete(userId: String, id: String)

    }
}