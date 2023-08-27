package com.codebrew.whrzat.ui.profile.favorite

import com.codebrew.whrzat.ui.profile.ProfileContract
import com.codebrew.whrzat.webservice.pojo.explore.FavoriteListData
import com.codebrew.whrzat.webservice.pojo.explore.HotspotData
import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData
import okhttp3.ResponseBody

class FavoriteContract {
    interface View{

        fun successFavorite(favoriteList: FavoriteListData)

        fun errorApi(errorBody: ResponseBody)

        fun errorFavoriteApi(errorBody: ResponseBody)
        fun failureApi()

        fun sessionExpire()

        fun dismissLoading()

        fun showLoading()
        fun noSpotFound()

    }
    interface Presenter{
        fun performFavoriteApi(userId: String)


        fun attachView(view: FavoriteContract.View)

        fun detachView()
    }
}