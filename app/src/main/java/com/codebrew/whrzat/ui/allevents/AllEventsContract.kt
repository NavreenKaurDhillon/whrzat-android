package com.codebrew.whrzat.ui.allevents

import com.codebrew.whrzat.webservice.pojo.allevents.EventList
import okhttp3.ResponseBody


class AllEventsContract {
    interface View{

        fun successEventsApi(events: List<EventList>)

        fun failureApi()

        fun errorApi(errorBody: ResponseBody)

        fun sessionExpire()

        fun dismissLoading()

        fun showLoading()
    }
    interface Presenter{

        fun getEvents(userID:String,hotspotId: String)

        fun attachView(view:View)

        fun detachView()
    }
}
