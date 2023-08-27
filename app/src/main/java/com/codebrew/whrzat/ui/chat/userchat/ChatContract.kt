package com.codebrew.whrzat.ui.chat.userchat

import com.codebrew.whrzat.webservice.pojo.chat.ChatMsgProfile
import okhttp3.ResponseBody


class ChatContract  {

    interface View{
        fun successChatReceiveApi(data: ChatMsgProfile)

        fun failureApi()

        fun errorApi(errorBody: ResponseBody)

        fun sessionExpire()

        fun userDeleted()
        fun showLoading()

        fun dismissLoading()

    }

    interface Presenter{

        fun receiveChatApi(userId:String,otherUserId:String)

        fun attachView(view:View)

        fun detachView()


    }
}