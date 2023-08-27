package com.codebrew.whrzat.ui.SeachExplore

import com.codebrew.whrzat.webservice.pojo.explore.EventListData
import com.codebrew.whrzat.webservice.pojo.explore.HotspotData
import com.codebrew.whrzat.webservice.pojo.explore.SendExploreData
import com.codebrew.whrzat.webservice.pojo.feed.FeedData
import com.codebrew.whrzat.webservice.pojo.feed.HappeningListData
import okhttp3.ResponseBody

class ExploreContracts {

    interface View {
        fun exploreList(exploreList: HotspotData)
        fun eventListSuccess(data: EventListData.Data)

        fun showLoading()

        fun dismissLoading()

        fun sessionExpired()

        fun failureExploreApi()

        fun errorExploreApi(errorBody: ResponseBody)
        fun noSpotFound()
        fun noEventFound()

        fun errorApi(errorBody: ResponseBody)

        fun failureApi()

    }

    interface Presenter {

        fun performExploreApi(sendExploreData: SendExploreData, lisLoader: Boolean, i: Int, page: Int)

        fun mapViewApi(sendExploreData: SendExploreData, lisLoader: Boolean, i: Int)
        fun performEventListApi(sendExploreData: SendExploreData, lisLoader: Boolean, i: Int, page: Int)

        fun performSearchApi(keyword: String)

        fun attachView(view: View)

        fun detachView()

    }
}