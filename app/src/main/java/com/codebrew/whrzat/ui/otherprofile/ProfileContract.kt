package com.codebrew.whrzat.ui.otherprofile

import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData
import okhttp3.ResponseBody

class ProfileContract{

    interface View{

        fun successProfileApi(data: UserData)

        fun errorApi(errorBody: ResponseBody)

        fun failureApi()

        fun sessionExpire()

        fun dismissLoading()

        fun showLoading()
        fun successBlockApi()
        fun  successLoveApi(likeCount: String, pos: Int)
        fun successReportApi()

    }
    interface Presenter{

        fun apiBlockProfile(userId :String,blockUserId:String,isBlock:Boolean)

        fun apiGetProfile(profileId:String,userId:String)

        fun love(userId:String, imageId:String, isLike:Boolean, likeCount: String, pos:Int)

        fun apiReport(imageId: String,userId: String)

        fun attachView(view:View)

        fun detachView()
    }
}
