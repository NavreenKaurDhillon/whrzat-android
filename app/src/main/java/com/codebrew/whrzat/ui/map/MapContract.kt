package com.codebrew.whrzat.ui.map

import com.codebrew.whrzat.webservice.pojo.explore.HotspotData
import com.codebrew.whrzat.webservice.pojo.explore.SendExploreData
import okhttp3.ResponseBody

class MapContract {
    interface View{
        fun mapData(data: HotspotData)
        fun noSpotAtMapFound()
        fun sessionExpired()
        fun failureExploreApi()
        fun errorExploreApi(errorBody: ResponseBody)
        fun noSpotFound()
        fun showLoading()

        fun dismissLoading()
    }

    interface Presenter {
        fun mapViewApi(sendExploreData: SendExploreData, lisLoader: Boolean, i: Int)

        fun attachView(view: View)

        fun detachView()

    }
}