package com.codebrew.whrzat.ui.feed.happening

import com.codebrew.whrzat.webservice.pojo.feed.HappeningListData
import okhttp3.ResponseBody

class FeedHappeningContract {
    interface View{

        fun successFeedHappeningApi(data: HappeningListData.Data)

        fun errorApi(errorBody: ResponseBody)
        fun errorReportApi(errorBody: ResponseBody)
        fun failureReportApi()

        fun failureApi()
        fun noEventFound()

        fun sessionExpire()

        fun dismissLoading()

        fun showLoading()
        fun successLoveApi()
        fun successReport()
    }
    interface Presenter{

        fun feedHappeningApi(page: Int,userId: String,radius:Int,lat:String,lng:String)

        fun attachView(view: View)

        fun apiReport(imageId: String,userId: String)

        fun love(userId:String, imageId:String, isLike:Boolean)

        fun detachView()
    }
}