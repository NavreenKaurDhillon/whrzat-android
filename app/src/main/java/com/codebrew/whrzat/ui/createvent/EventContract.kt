package com.codebrew.whrzat.ui.createvent

import com.codebrew.whrzat.webservice.pojo.HotspotDetail.SendEventData
import okhttp3.ResponseBody


class EventContract {
    interface View{
        fun successEventApi()

        fun errorEventApi(errorBody: ResponseBody)

        fun failureEventApi()

        fun sessionExpire()

        fun invalidName()

        fun invalidDescription()

        fun invalidStartDate()

        fun invalidEndDate()

        fun showLoading()

        fun dismissLoading()
    }

    interface Presenter{
        fun createEventApi(sendEventData: SendEventData)

        fun attachView(view:View)

        fun detachView()
    }
}