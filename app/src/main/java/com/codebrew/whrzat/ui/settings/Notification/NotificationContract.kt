package com.codebrew.whrzat.ui.settings.Notification

import com.codebrew.whrzat.webservice.pojo.ApiNotification
import okhttp3.ResponseBody

class NotificationContract{
    interface  View{
        fun successNotificationApi(data: List<String>)

        fun failureNotificationApi()

        fun errorNotificationApi(errorBody: ResponseBody)

        fun sessionExpire()

        fun showLoading()

        fun dismissLoading()
        fun notificationListing(data: List<String>)
    }

    interface Presenter{

        fun notificationApi(notificationData: ApiNotification)

        fun attachView(view:View)

        fun detachView()
        fun getNotificationListing(userId: String)
    }
}
