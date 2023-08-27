package com.codebrew.whrzat.ui.detailhotspot

import com.codebrew.whrzat.webservice.pojo.HotspotDetail.DetailData
import com.codebrew.whrzat.webservice.pojo.HotspotDetail.HotSpotDetail
import okhttp3.ResponseBody
import java.io.File


class DetailContract {
    interface View {
        fun successHotspotDetailApi(hotspotData: HotSpotDetail)

        fun successAddImagesApi()

        fun successAddEvent()

        fun successCheckInApi(data: DetailData?)

        fun successCheckOutApi(data:DetailData?)

        fun successReportApi()

        fun successFavoriteApi()

        fun failureApi()

        fun errorApi(errorBody: ResponseBody)

        fun dismissLoading()

        fun showLoading()

        fun sessionExpire()

        fun successLoveApi()
        fun successDeleteApi()
        fun errorMassage(it: ResponseBody)
        fun Apimesaage(message: String?)

    }

    interface Presenter {

        fun getHotspotDetail(hotspotId: String, userId: String)

        fun checkIn(hotspotId: String, userId: String)
        fun favoriteHotSpot(hotspotId: String, userId: String,status:Boolean)

        fun checkOut(hotspotId: String, userId: String)

        fun addImages(image: File, createdBy: String, hotspotId: String)

        fun love(userId: String, imageId: String, isLike: Boolean)

        fun apiReport(imageId: String,userId: String)

        fun apiDelete(imageId: String, hotspotId: String)


        fun attachView(view: View)

        fun detachView()



    }
}