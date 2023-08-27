package com.codebrew.whrzat.ui.settings.Notification

import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.NotificationListing
import com.codebrew.whrzat.webservice.pojo.ApiNotification
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationPresenter : NotificationContract.Presenter {

    private var view: NotificationContract.View? = null
    override fun notificationApi(notificationData: ApiNotification) {
        view?.showLoading()
        RetrofitClient.get().apiEditNotification(notificationData).enqueue(object : Callback<NotificationListing> {
            override fun onResponse(call: Call<NotificationListing>, response: Response<NotificationListing>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { view?.successNotificationApi(it) }
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorNotificationApi(it) }
                    }

                }
                view?.dismissLoading()
            }


            override fun onFailure(call: Call<NotificationListing>, t: Throwable) {
                view?.failureNotificationApi()
                view?.dismissLoading()

            }


        })
    }

    override fun getNotificationListing(userId: String) {
        view?.showLoading()
        RetrofitClient.get().apiNotificationList(userId).enqueue(object : Callback<NotificationListing> {
            override fun onResponse(call: Call<NotificationListing>, response: Response<NotificationListing>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { view?.notificationListing(it) }
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorNotificationApi(it) }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<NotificationListing>, t: Throwable) {
                view?.dismissLoading()
                view?.failureNotificationApi()

            }
        })

    }

    override fun attachView(view: NotificationContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

}
