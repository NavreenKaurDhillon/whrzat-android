package com.codebrew.whrzat.ui.profile

import com.codebrew.whrzat.webservice.pojo.explore.HotspotData
import com.codebrew.whrzat.webservice.pojo.explore.SendExploreData
import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData
import okhttp3.ResponseBody

class ProfileContract{

    interface View{

        fun successProfileApi(data: UserData)
        fun favoriteList(exploreList: HotspotData)

        fun errorApi(errorBody: ResponseBody)

        fun failureApi()

        fun sessionExpire()

        fun dismissLoading()

        fun showLoading()

        fun successBlockApi()

        fun  successLoveApi(likeCount: String, pos: Int)


        fun errorFavoriteApi(errorBody: ResponseBody)
        fun noSpotFound()

        fun failureFavoriteApi()

    }
    interface Presenter{


        fun apiGetProfile(profileId:String,userId:String)

        fun love(userId:String, imageId:String, isLike:Boolean, likeCount: String, pos:Int)
        fun performFavoriteApi(userId: String)


        fun attachView(view:View)

        fun detachView()
    }
}