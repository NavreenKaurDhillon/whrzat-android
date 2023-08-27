package com.codebrew.whrzat.ui.settings.blocked

import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData
import okhttp3.ResponseBody

class BlockedContract{

    interface View{

        fun successBlockedApi(data: List<UserData>)

        fun sessionExpired()

        fun errorBlockedApi(errorBody: ResponseBody)

        fun failureBlockedApi()

        fun showLoading()

        fun dismissLoading()
        fun successUnBlock()
    }
    interface Presenter{
        fun apiBlockList(userId:String)

        fun apiUnBlock(userId:String,otherUserId:String)


        fun attachView(view:View)

        fun detachView()
    }
}
