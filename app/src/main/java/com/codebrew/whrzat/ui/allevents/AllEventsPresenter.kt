package com.codebrew.whrzat.ui.allevents

import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.allevents.EventMain
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AllEventsPresenter : AllEventsContract.Presenter {

    private var view: AllEventsContract.View? = null
    override fun getEvents(userID: String, hotspotId: String) {
        view?.showLoading()
        RetrofitClient.get().apiGetEvents(hotspotId, userID).enqueue(object : Callback<EventMain> {
            override fun onResponse(call: Call<EventMain>, response: Response<EventMain>) {
                if (response.isSuccessful) {
                    response.body()?.data?.events?.let { view?.successEventsApi(it) }
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<EventMain>, t: Throwable) {
                view?.failureApi()
                view?.dismissLoading()
            }
        })
    }

    override fun attachView(view: AllEventsContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}