package com.codebrew.whrzat.ui.feed.contacts

import com.codebrew.whrzat.webservice.pojo.ApiContacts
import com.codebrew.whrzat.webservice.pojo.feed.FeedData
import okhttp3.ResponseBody

class FeedContactContract{

    interface View{

        fun successFeedApi(data: List<FeedData>)

        fun errorApi(errorBody: ResponseBody)

        fun failureApi()

        fun sessionExpire()

        fun dismissLoading()

        fun showLoading()
        fun successLoveApi()
        fun successReport()
        fun apiSuccessContacts()
    }
    interface Presenter{

        fun feedApi(userId:String,isLoader:Boolean)

        fun attachView(view: View)

        fun apiReport(imageId: String,userId: String)

        fun love(userId:String, imageId:String, isLike:Boolean)

        fun detachView()
        fun apiSyncContacts(contactsObj: ApiContacts)
    }
}
