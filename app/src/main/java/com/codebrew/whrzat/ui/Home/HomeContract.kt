package com.codebrew.whrzat.ui.Home

import com.codebrew.whrzat.webservice.pojo.ApiContacts
import com.codebrew.whrzat.webservice.pojo.explore.EventListData
import com.codebrew.whrzat.webservice.pojo.feed.HappeningListData
import com.codebrew.whrzat.webservice.pojo.explore.HotspotData
import com.codebrew.whrzat.webservice.pojo.explore.SendExploreData
import com.codebrew.whrzat.webservice.pojo.feed.FeedData
import okhttp3.ResponseBody

class HomeContract {
    interface View {
        fun exploreList(exploreList: HotspotData)
        fun eventListSuccess(data: EventListData.Data)
        fun successFeedHappeningApi(data: HappeningListData.Data)
        fun showLoading()

        fun dismissLoading()

        fun sessionExpired()

        fun failureExploreApi()

        fun errorExploreApi(errorBody: ResponseBody)
        fun noSpotFound()
        fun noEventFound()
        fun mapData(data: HotspotData)
        fun noSpotAtMapFound()
        fun errorApi(errorBody: ResponseBody)
        fun successLoveApi()
        fun successReport()
        fun errorReportApi(errorBody: ResponseBody)
        fun failureReportApi()
        fun failureApi(message: String?)
        fun successFeedApi(data: List<FeedData>)
        fun apiSuccessContacts()
    }

    interface Presenter {
        fun feedHappeningApi(page: Int,userId: String,radius:Int,lat:String,lng:String)
        fun feedApi(userId:String,isLoader:Boolean)

        fun performExploreApi(sendExploreData: SendExploreData, lisLoader: Boolean, i: Int, page: Int)

        fun mapViewApi(sendExploreData: SendExploreData, lisLoader: Boolean, i: Int)
        fun performEventListApi(sendExploreData: SendExploreData, lisLoader: Boolean, i: Int, page: Int)

        fun performSearchApi(keyword: String)

        fun attachView(view: View)

        fun detachView()
        fun apiReport(imageId: String,userId: String)


        fun love(userId:String, imageId:String, isLike:Boolean)
        fun apiSyncContacts(contactsObj: ApiContacts) {

        }
    }
}
