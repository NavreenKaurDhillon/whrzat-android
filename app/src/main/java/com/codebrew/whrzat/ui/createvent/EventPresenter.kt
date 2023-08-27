package com.codebrew.whrzat.ui.createvent

import android.util.Log
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.HotspotDetail.DetailData
import com.codebrew.whrzat.webservice.pojo.HotspotDetail.SendEventData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EventPresenter : EventContract.Presenter {
    private var view: EventContract.View? = null
    private  val TAG = "EventPresenter"
    override fun createEventApi(sendEventData: SendEventData) {
        Log.d(TAG, "createEventApi: StartActivity")

        if (sendEventData.name.isEmpty()) {
            view?.invalidName()
        } else if (sendEventData.description.isEmpty()) {
            view?.invalidDescription()
        } /*else if (sendEventData.startDate!=null) {
            view?.invalidStartDate()
        } else if (sendEventData.endDate==0) {
            view?.invalidEndDate()
        }*/ else {
            val map = HashMap<String, String>()
            map.put(ApiConstants.NAME, sendEventData.name)
            map.put(ApiConstants.DESCRIPTION, sendEventData.description)
            map.put(ApiConstants.START_DATE, sendEventData.startDate.toString())
            map.put(ApiConstants.END_DATE, sendEventData.endDate.toString())
            map.put(ApiConstants.CREATED_BY, sendEventData.createdBy)
            map.put(ApiConstants.HOTSPOT_ID, sendEventData.hotspotId)


            view?.showLoading()
            RetrofitClient.get().apiCreateEvent(map).enqueue(object :Callback<DetailData>{
                override fun onResponse(call: Call<DetailData>, response: Response<DetailData>) {
                    if(response.isSuccessful){
                        view?.successEventApi()
                    }else{
                        if(response.code()==ApiConstants.UNAUTHORIZED_ACCESS) {
                            view?.sessionExpire()
                        }else{
                            response.errorBody()?.let { view?.errorEventApi(it) }
                        }
                    }

                    view?.dismissLoading()

                }

                override fun onFailure(call: Call<DetailData>, t: Throwable) {
                    view?.dismissLoading()
                    view?.failureEventApi()
                }
            })
        }


    }

    override fun attachView(view: EventContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null

    }
}